package com.sigl.projets.application;

import com.sigl.projets.application.TeamService.Actor;
import com.sigl.projets.domain.ForbiddenActionException;
import com.sigl.projets.domain.NotFoundException;
import com.sigl.projets.domain.Project;
import com.sigl.projets.domain.ProjectAction;
import com.sigl.projets.infrastructure.ProjectRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectLifecycleService {

  private final ProjectRepository projects;
  private final OpenWorkPort openWork;

  public ProjectLifecycleService(ProjectRepository projects, OpenWorkPort openWork) {
    this.projects = projects;
    this.openWork = openWork;
  }

  @Transactional
  public Project apply(Actor actor, UUID projectId, ProjectAction action, String reason) {
    Project project =
        projects.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));
    if (!actor.admin() && !project.isManagedBy(actor.userId())) {
      throw new ForbiddenActionException("Only the project manager changes the project state");
    }
    boolean needsGuard = action == ProjectAction.CLOSE;
    project.apply(action, actor.admin(), reason, needsGuard && openWork.hasOpenWork(projectId));
    return project;
  }
}
