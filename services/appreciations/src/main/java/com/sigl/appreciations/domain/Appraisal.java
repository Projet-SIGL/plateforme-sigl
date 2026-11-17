package com.sigl.appreciations.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** An appraisal of an apprentice: DRAFT, then SUBMITTED, then VALIDATED (state diagram, Part 1 §5). */
@Entity
@Table(name = "appraisal")
public class Appraisal {

  @Id private UUID id;

  @Column(name = "apprentice_id", nullable = false)
  private UUID apprenticeId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "evaluator_id", nullable = false)
  private UUID evaluatorId;

  @Column(nullable = false)
  private String period;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AppraisalStatus status;

  @ElementCollection
  @CollectionTable(name = "appraisal_score", joinColumns = @JoinColumn(name = "appraisal_id"))
  @MapKeyColumn(name = "criterion_id")
  @Column(name = "score")
  private Map<UUID, Integer> scores = new HashMap<>();

  protected Appraisal() {}

  private Appraisal(UUID apprenticeId, UUID projectId, UUID evaluatorId, String period) {
    this.id = UUID.randomUUID();
    this.apprenticeId = apprenticeId;
    this.projectId = projectId;
    this.evaluatorId = evaluatorId;
    this.period = period;
    this.status = AppraisalStatus.DRAFT;
  }

  public static Appraisal draft(
      UUID apprenticeId, UUID projectId, UUID evaluatorId, String period) {
    return new Appraisal(apprenticeId, projectId, evaluatorId, period);
  }

  /** Replaces the scores of a draft; a submitted or validated appraisal is frozen. */
  public void replaceScores(Map<UUID, Integer> newScores) {
    if (status != AppraisalStatus.DRAFT) {
      throw new IllegalTransitionException("Scores can only be changed on a DRAFT appraisal");
    }
    scores.clear();
    scores.putAll(newScores);
  }

  /** DRAFT to SUBMITTED by the author, only with a complete grid. */
  public void submit(UUID actorId, Map<UUID, Criterion> requiredCriteria) {
    if (!evaluatorId.equals(actorId)) {
      throw new ForbiddenActionException("Only the author can submit the appraisal");
    }
    if (status != AppraisalStatus.DRAFT) {
      throw new IllegalTransitionException("Cannot submit an appraisal in state " + status);
    }
    Set<UUID> required = requiredCriteria.keySet();
    if (required.isEmpty() || !scores.keySet().containsAll(required)) {
      throw new InvalidAppraisalException("The grid is incomplete");
    }
    for (Criterion criterion : requiredCriteria.values()) {
      int score = scores.get(criterion.getId());
      if (score < 0 || score > criterion.getMaxScore()) {
        throw new InvalidAppraisalException(
            "Score out of range for criterion " + criterion.getLabel());
      }
    }
    status = AppraisalStatus.SUBMITTED;
  }

  /** SUBMITTED to VALIDATED, triggered by the validation of the summary sheet (hypothesis H-D02). */
  public void validate() {
    if (status != AppraisalStatus.SUBMITTED) {
      throw new IllegalTransitionException("Cannot validate an appraisal in state " + status);
    }
    status = AppraisalStatus.VALIDATED;
  }

  public UUID getId() {
    return id;
  }

  public UUID getApprenticeId() {
    return apprenticeId;
  }

  public UUID getProjectId() {
    return projectId;
  }

  public UUID getEvaluatorId() {
    return evaluatorId;
  }

  public String getPeriod() {
    return period;
  }

  public AppraisalStatus getStatus() {
    return status;
  }

  public Map<UUID, Integer> getScores() {
    return Map.copyOf(scores);
  }
}
