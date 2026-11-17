package com.sigl.appreciations.application;

import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import com.sigl.appreciations.domain.Criterion;
import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.NotFoundException;
import com.sigl.appreciations.infrastructure.AppraisalRepository;
import com.sigl.appreciations.infrastructure.CriterionRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppraisalService {

  public record NewAppraisal(
      UUID apprenticeId, UUID projectId, String period, Map<UUID, Integer> scores) {}

  private final AppraisalRepository appraisals;
  private final CriterionRepository criteria;
  private final SupervisionPort supervision;

  public AppraisalService(
      AppraisalRepository appraisals, CriterionRepository criteria, SupervisionPort supervision) {
    this.appraisals = appraisals;
    this.criteria = criteria;
    this.supervision = supervision;
  }

  /** Creates a draft after checking effective supervision (rule R3), whatever the caller's role. */
  @Transactional
  public Appraisal createDraft(UUID evaluatorId, NewAppraisal request) {
    requireSupervision(evaluatorId, request.apprenticeId(), request.projectId());
    Appraisal appraisal =
        Appraisal.draft(
            request.apprenticeId(), request.projectId(), evaluatorId, request.period());
    if (request.scores() != null) {
      appraisal.replaceScores(request.scores());
    }
    return appraisals.save(appraisal);
  }

  /** Submits a complete draft; R3 is checked again at submission time. */
  @Transactional
  public Appraisal submit(UUID actorId, UUID appraisalId) {
    Appraisal appraisal =
        appraisals
            .findById(appraisalId)
            .orElseThrow(() -> new NotFoundException("Appraisal not found"));
    if (!appraisal.getEvaluatorId().equals(actorId)) {
      throw new ForbiddenActionException("Only the author can submit the appraisal");
    }
    requireSupervision(actorId, appraisal.getApprenticeId(), appraisal.getProjectId());
    Map<UUID, Criterion> required =
        criteria.findByActiveTrue().stream()
            .collect(Collectors.toMap(Criterion::getId, Function.identity()));
    appraisal.submit(actorId, required);
    return appraisal;
  }

  /**
   * Appraisals of an apprentice. The apprentice only sees validated ones (hypothesis H-D02); staff
   * see all of them.
   */
  @Transactional(readOnly = true)
  public List<Appraisal> listFor(UUID apprenticeId, UUID requesterId, boolean staff) {
    if (!staff && !apprenticeId.equals(requesterId)) {
      throw new ForbiddenActionException("An apprentice only reads their own appraisals");
    }
    List<Appraisal> all = appraisals.findByApprenticeId(apprenticeId);
    if (staff) {
      return all;
    }
    return all.stream().filter(a -> a.getStatus() == AppraisalStatus.VALIDATED).toList();
  }

  private void requireSupervision(UUID evaluatorId, UUID apprenticeId, UUID projectId) {
    if (!supervision.hasSupervised(evaluatorId, apprenticeId, projectId)) {
      throw new ForbiddenActionException(
          "The role is not enough: the evaluator must have supervised the apprentice (R3)");
    }
  }
}
