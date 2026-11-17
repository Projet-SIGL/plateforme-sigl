package com.sigl.apprentis.api;

import com.sigl.apprentis.application.ApprenticeService.Viewer;
import com.sigl.apprentis.application.SchoolContactService;
import com.sigl.apprentis.domain.SchoolContact;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ecoles/{schoolId}/contacts")
class SchoolContactController {

  record ContactRequest(String name, String email, String phone) {}

  record ContactResponse(UUID id, UUID schoolId, String name, String email, String phone) {
    static ContactResponse of(SchoolContact c) {
      return new ContactResponse(c.getId(), c.getSchoolId(), c.getName(), c.getEmail(), c.getPhone());
    }
  }

  private final SchoolContactService service;

  SchoolContactController(SchoolContactService service) {
    this.service = service;
  }

  @PutMapping("/{contactId}")
  @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'SCHOOL')")
  ContactResponse update(
      Authentication auth,
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID schoolId,
      @PathVariable UUID contactId,
      @RequestBody ContactRequest body) {
    boolean school = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SCHOOL"));
    String claim = jwt.getClaimAsString("school_id");
    Viewer viewer = new Viewer(school, claim == null ? null : UUID.fromString(claim));
    return ContactResponse.of(
        service.update(viewer, schoolId, contactId, body.name(), body.email(), body.phone()));
  }
}
