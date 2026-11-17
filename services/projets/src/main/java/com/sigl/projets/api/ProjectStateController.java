package com.sigl.projets.api;

import com.sigl.projets.application.ProjectLifecycleService;
import com.sigl.projets.application.TeamService.Actor;
import com.sigl.projets.domain.Project;
import com.sigl.projets.domain.ProjectAction;
import com.sigl.projets.domain.ProjectStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projets")
class ProjectStateController {

  record StateRequest(@NotNull ProjectAction action, String reason) {}

  record StateResponse(UUID id, ProjectStatus status) {}

  private final ProjectLifecycleService service;

  ProjectStateController(ProjectLifecycleService service) {
    this.service = service;
  }

  @PatchMapping("/{id}/etat")
  @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
  StateResponse change(
      Authentication auth,
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody StateRequest request) {
    boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    Project project =
        service.apply(new Actor(UUID.fromString(jwt.getSubject()), admin), id, request.action(), request.reason());
    return new StateResponse(project.getId(), project.getStatus());
  }
}
