package com.sigl.projets.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.sigl.projets.application.TeamService.Actor;
import com.sigl.projets.domain.ForbiddenActionException;
import com.sigl.projets.domain.IllegalTransitionException;
import com.sigl.projets.domain.NotFoundException;
import com.sigl.projets.domain.Project;
import com.sigl.projets.domain.ProjectAction;
import com.sigl.projets.domain.ProjectStatus;
import com.sigl.projets.infrastructure.ProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectLifecycleServiceTest {

  @Mock ProjectRepository projects;
  @Mock OpenWorkPort openWork;

  private ProjectLifecycleService service;
  private final UUID manager = UUID.randomUUID();
  private final Project project = new Project(manager, "Platform");

  @BeforeEach
  void setUp() {
    service = new ProjectLifecycleService(projects, openWork);
    when(projects.findById(project.getId())).thenReturn(Optional.of(project));
    project.apply(ProjectAction.START, false, null, false);
  }

  @Test
  void theManagerClosesThenArchives() {
    Actor actor = new Actor(manager, false);

    service.apply(actor, project.getId(), ProjectAction.CLOSE, null);
    service.apply(actor, project.getId(), ProjectAction.ARCHIVE, null);

    assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
  }

  @Test
  void closingIsRefusedWhileTasksAreOpen() {
    when(openWork.hasOpenWork(project.getId())).thenReturn(true);

    assertThatThrownBy(
            () -> service.apply(new Actor(manager, false), project.getId(), ProjectAction.CLOSE, null))
        .isInstanceOf(IllegalTransitionException.class);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
  }

  @Test
  void anotherManagerCannotChangeTheState() {
    assertThatThrownBy(
            () -> service.apply(new Actor(UUID.randomUUID(), false), project.getId(), ProjectAction.CLOSE, null))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void anAdminReopensAClosedProject() {
    service.apply(new Actor(manager, false), project.getId(), ProjectAction.CLOSE, null);

    service.apply(new Actor(UUID.randomUUID(), true), project.getId(), ProjectAction.REOPEN, "mistake");

    assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
  }

  @Test
  void reportsAnUnknownProject() {
    assertThatThrownBy(
            () -> service.apply(new Actor(manager, false), UUID.randomUUID(), ProjectAction.CLOSE, null))
        .isInstanceOf(NotFoundException.class);
  }
}
