package com.sigl.projets.api;

import com.sigl.projets.application.TeamService;
import com.sigl.projets.application.TeamService.Actor;
import com.sigl.projets.domain.Team;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projets/{projectId}/equipes")
class TeamController {

  record CreateTeamRequest(@NotBlank String name) {}

  record MembersRequest(@NotNull Set<UUID> members) {}

  record TeamResponse(UUID id, UUID projectId, String name, Set<UUID> members) {
    static TeamResponse of(Team t) {
      return new TeamResponse(t.getId(), t.getProjectId(), t.getName(), t.getMemberIds());
    }
  }

  private final TeamService service;

  TeamController(TeamService service) {
    this.service = service;
  }

  private static Actor actor(Authentication auth, Jwt jwt) {
    boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    return new Actor(UUID.fromString(jwt.getSubject()), admin);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
  TeamResponse create(
      Authentication auth,
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID projectId,
      @Valid @RequestBody CreateTeamRequest request) {
    return TeamResponse.of(service.create(actor(auth, jwt), projectId, request.name()));
  }

  @PutMapping("/{teamId}/membres")
  @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'ADMIN')")
  TeamResponse replaceMembers(
      Authentication auth,
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID projectId,
      @PathVariable UUID teamId,
      @Valid @RequestBody MembersRequest request) {
    return TeamResponse.of(
        service.replaceMembers(actor(auth, jwt), projectId, teamId, request.members()));
  }
}
