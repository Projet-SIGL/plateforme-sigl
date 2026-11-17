package com.sigl.appreciations.api;

import com.sigl.appreciations.application.SummaryService;
import com.sigl.appreciations.application.SummaryService.Reader;
import com.sigl.appreciations.domain.AppraisalSummary;
import com.sigl.appreciations.domain.SummaryStatus;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/syntheses")
class SummaryController {

  record GenerateRequest(@NotNull UUID apprenticeId, @NotBlank String period) {}

  record SummaryResponse(
      UUID id, UUID apprenticeId, String period, SummaryStatus status, Set<UUID> appraisalIds) {
    static SummaryResponse of(AppraisalSummary s) {
      return new SummaryResponse(
          s.getId(), s.getApprenticeId(), s.getPeriod(), s.getStatus(), s.getAppraisalIds());
    }
  }

  private final SummaryService service;

  SummaryController(SummaryService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('HR')")
  SummaryResponse generate(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody GenerateRequest request) {
    return SummaryResponse.of(
        service.generate(UUID.fromString(jwt.getSubject()), request.apprenticeId(), request.period()));
  }

  @PostMapping("/{id}/validation")
  @PreAuthorize("hasRole('MASTER')")
  SummaryResponse validate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return SummaryResponse.of(service.validate(UUID.fromString(jwt.getSubject()), id));
  }

  @PostMapping("/{id}/transmission")
  @PreAuthorize("hasRole('HR')")
  SummaryResponse transmit(@PathVariable UUID id) {
    return SummaryResponse.of(service.transmit(id));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('HR', 'MASTER', 'ADMIN', 'SCHOOL')")
  SummaryResponse read(Authentication auth, @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    boolean school = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SCHOOL"));
    String schoolClaim = jwt.getClaimAsString("school_id");
    Reader reader =
        new Reader(
            UUID.fromString(jwt.getSubject()),
            school,
            schoolClaim == null ? null : UUID.fromString(schoolClaim));
    return SummaryResponse.of(service.read(reader, id));
  }
}
