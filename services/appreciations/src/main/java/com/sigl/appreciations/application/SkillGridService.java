package com.sigl.appreciations.application;

import com.sigl.appreciations.domain.Criterion;
import com.sigl.appreciations.domain.InvalidAppraisalException;
import com.sigl.appreciations.infrastructure.CriterionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillGridService {

  public record CriterionDraft(String label, int maxScore) {}

  private final CriterionRepository criteria;

  public SkillGridService(CriterionRepository criteria) {
    this.criteria = criteria;
  }

  @Transactional(readOnly = true)
  public List<Criterion> activeGrid() {
    return criteria.findByActiveTrue();
  }

  /** Replaces the common grid: previous criteria are deactivated, never deleted. */
  @Transactional
  public List<Criterion> replaceGrid(List<CriterionDraft> drafts) {
    if (drafts == null || drafts.isEmpty()) {
      throw new InvalidAppraisalException("A grid needs at least one criterion");
    }
    criteria.findByActiveTrue().forEach(Criterion::deactivate);
    List<Criterion> created =
        drafts.stream().map(d -> new Criterion(UUID.randomUUID(), d.label(), d.maxScore())).toList();
    return criteria.saveAll(created);
  }
}
