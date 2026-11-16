package com.sigl.appreciations.api;

import com.sigl.appreciations.application.SkillGridService;
import com.sigl.appreciations.application.SkillGridService.CriterionDraft;
import com.sigl.appreciations.domain.Criterion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grilles")
class SkillGridController {

  record CriterionRequest(@NotBlank String label, @Min(1) int maxScore) {}

  record GridRequest(@NotEmpty List<@Valid CriterionRequest> criteria) {}

  record CriterionResponse(UUID id, String label, int maxScore) {
    static CriterionResponse of(Criterion c) {
      return new CriterionResponse(c.getId(), c.getLabel(), c.getMaxScore());
    }
  }

  private final SkillGridService grid;

  SkillGridController(SkillGridService grid) {
    this.grid = grid;
  }

  @GetMapping
  List<CriterionResponse> read() {
    return grid.activeGrid().stream().map(CriterionResponse::of).toList();
  }

  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  List<CriterionResponse> replace(@Valid @RequestBody GridRequest request) {
    List<CriterionDraft> drafts =
        request.criteria().stream().map(c -> new CriterionDraft(c.label(), c.maxScore())).toList();
    return grid.replaceGrid(drafts).stream().map(CriterionResponse::of).toList();
  }
}
