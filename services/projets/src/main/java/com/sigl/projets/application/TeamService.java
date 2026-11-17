package com.sigl.projets.application;

import com.sigl.projets.domain.ForbiddenActionException;
import com.sigl.projets.domain.IllegalTransitionException;
import com.sigl.projets.domain.InvalidRequestException;
import com.sigl.projets.domain.NotFoundException;
import com.sigl.projets.domain.Project;
import com.sigl.projets.domain.Team;
import com.sigl.projets.infrastructure.ProjectRepository;
import com.sigl.projets.infrastructure.TeamRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

  /** The caller: user id and whether they are an administrator. */
  public record Actor(UUID userId, boolean admin) {}

  private final ProjectRepository projects;
  private final TeamRepository teams;

  public TeamService(ProjectRepository projects, TeamRepository teams) {
    this.projects = projects;
    this.teams = teams;
  }

  @Transactional
  public Team create(Actor actor, UUID projectId, String name) {
    Project project = editableProject(actor, projectId);
    String clean = name == null ? "" : name.trim();
    if (teams.existsByProjectIdAndName(project.getId(), clean)) {
      throw new InvalidRequestException("A team with this name already exists in the project");
    }
    return teams.save(new Team(project.getId(), clean));
  }

  @Transactional
  public Team replaceMembers(Actor actor, UUID projectId, UUID teamId, Set<UUID> members) {
    Project project = editableProject(actor, projectId);
    Team team =
        teams
            .findByIdAndProjectId(teamId, project.getId())
            .orElseThrow(() -> new NotFoundException("Team not found in this project"));
    team.replaceMembers(members == null ? Set.of() : members);
    return team;
  }

  private Project editableProject(Actor actor, UUID projectId) {
    Project project =
        projects.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));
    if (!actor.admin() && !project.isManagedBy(actor.userId())) {
      throw new ForbiddenActionException("Only the project manager manages the teams");
    }
    if (project.isFrozen()) {
      throw new IllegalTransitionException("The project is closed: its teams cannot change");
    }
    return project;
  }
}
