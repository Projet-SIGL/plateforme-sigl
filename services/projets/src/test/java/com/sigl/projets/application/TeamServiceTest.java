package com.sigl.projets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sigl.projets.application.TeamService.Actor;
import com.sigl.projets.domain.ForbiddenActionException;
import com.sigl.projets.domain.IllegalTransitionException;
import com.sigl.projets.domain.InvalidRequestException;
import com.sigl.projets.domain.NotFoundException;
import com.sigl.projets.domain.Project;
import com.sigl.projets.domain.Team;
import com.sigl.projets.infrastructure.ProjectRepository;
import com.sigl.projets.infrastructure.TeamRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamServiceTest {

  @Mock ProjectRepository projects;
  @Mock TeamRepository teams;

  private TeamService service;
  private final UUID manager = UUID.randomUUID();
  private final Project project = new Project(manager, "Platform");

  @BeforeEach
  void setUp() {
    service = new TeamService(projects, teams);
    when(projects.findById(project.getId())).thenReturn(Optional.of(project));
    when(teams.save(any(Team.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void theManagerCreatesATeam() {
    Team team = service.create(new Actor(manager, false), project.getId(), "  Back-end ");

    assertThat(team.getName()).isEqualTo("Back-end");
    assertThat(team.getProjectId()).isEqualTo(project.getId());
  }

  @Test
  void anAdminCreatesATeamOnAnyProject() {
    assertThat(service.create(new Actor(UUID.randomUUID(), true), project.getId(), "QA")).isNotNull();
  }

  @Test
  void anotherManagerCannot() {
    assertThatThrownBy(() -> service.create(new Actor(UUID.randomUUID(), false), project.getId(), "QA"))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void refusesADuplicateNameInTheSameProject() {
    when(teams.existsByProjectIdAndName(project.getId(), "QA")).thenReturn(true);

    assertThatThrownBy(() -> service.create(new Actor(manager, false), project.getId(), "QA"))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void refusesABlankName() {
    assertThatThrownBy(() -> service.create(new Actor(manager, false), project.getId(), "  "))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void refusesAnUnknownProject() {
    assertThatThrownBy(() -> service.create(new Actor(manager, false), UUID.randomUUID(), "QA"))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void aClosedProjectIsFrozen() {
    ReflectionTestUtils.setField(project, "status", com.sigl.projets.domain.ProjectStatus.CLOSED);

    assertThatThrownBy(() -> service.create(new Actor(manager, false), project.getId(), "QA"))
        .isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void replacesTheMembers() {
    Team team = new Team(project.getId(), "QA");
    when(teams.findByIdAndProjectId(team.getId(), project.getId())).thenReturn(Optional.of(team));
    UUID alice = UUID.randomUUID();
    UUID bob = UUID.randomUUID();

    service.replaceMembers(new Actor(manager, false), project.getId(), team.getId(), Set.of(alice, bob));
    service.replaceMembers(new Actor(manager, false), project.getId(), team.getId(), Set.of(bob));

    assertThat(team.getMemberIds()).containsExactly(bob);
  }

  @Test
  void aTeamOfAnotherProjectIsNotFound() {
    UUID teamId = UUID.randomUUID();
    when(teams.findByIdAndProjectId(teamId, project.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.replaceMembers(new Actor(manager, false), project.getId(), teamId, Set.of()))
        .isInstanceOf(NotFoundException.class);
  }
}
