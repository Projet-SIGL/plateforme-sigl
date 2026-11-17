package com.sigl.apprentis.api;

import com.sigl.apprentis.application.ApprenticeService;
import com.sigl.apprentis.application.ApprenticeService.Viewer;
import com.sigl.apprentis.domain.Apprentice;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apprentis")
class ApprenticeController {

  record ApprenticeResponse(UUID id, String name, UUID schoolId, UUID masterId) {
    static ApprenticeResponse of(Apprentice a) {
      return new ApprenticeResponse(a.getId(), a.getName(), a.getSchoolId(), a.getMasterId());
    }
  }

  private final ApprenticeService service;

  ApprenticeController(ApprenticeService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGEMENT', 'PROJECT_MANAGER', 'MASTER', 'SCHOOL')")
  List<ApprenticeResponse> search(
      Authentication auth, @AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String recherche) {
    boolean school = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SCHOOL"));
    String claim = jwt.getClaimAsString("school_id");
    Viewer viewer = new Viewer(school, claim == null ? null : UUID.fromString(claim));
    return service.search(viewer, recherche).stream().map(ApprenticeResponse::of).toList();
  }
}
