package com.sigl.appreciations.api;

import com.sigl.appreciations.application.AppraisalService;
import com.sigl.appreciations.application.AppraisalService.NewAppraisal;
import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appreciations")
class AppraisalController {

  record CreateRequest(
      @NotNull UUID apprenticeId,
      @NotNull UUID projectId,
      @NotBlank String period,
      Map<UUID, Integer> scores) {}

  record AppraisalResponse(
      UUID id,
      UUID apprenticeId,
      UUID projectId,
      UUID evaluatorId,
      String period,
      AppraisalStatus status,
      Map<UUID, Integer> scores) {
    static AppraisalResponse of(Appraisal a) {
      return new AppraisalResponse(
          a.getId(),
          a.getApprenticeId(),
          a.getProjectId(),
          a.getEvaluatorId(),
          a.getPeriod(),
          a.getStatus(),
          a.getScores());
    }
  }

  private final AppraisalService service;

  AppraisalController(AppraisalService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'MASTER')")
  AppraisalResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateRequest request) {
    UUID evaluatorId = UUID.fromString(jwt.getSubject());
    return AppraisalResponse.of(
        service.createDraft(
            evaluatorId,
            new NewAppraisal(
                request.apprenticeId(), request.projectId(), request.period(), request.scores())));
  }

  @PostMapping("/{id}/soumission")
  @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'MASTER')")
  AppraisalResponse submit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return AppraisalResponse.of(service.submit(UUID.fromString(jwt.getSubject()), id));
  }
}
