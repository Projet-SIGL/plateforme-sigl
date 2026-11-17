package com.sigl.appreciations.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Summary sheet for the school: GENERATED, then VALIDATED by the master, then SENT. */
@Entity
@Table(name = "appraisal_summary")
public class AppraisalSummary {

  @Id private UUID id;

  @Column(name = "apprentice_id", nullable = false)
  private UUID apprenticeId;

  @Column(nullable = false)
  private String period;

  @Column(name = "generated_by", nullable = false)
  private UUID generatedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SummaryStatus status;

  @ElementCollection
  @CollectionTable(name = "appraisal_summary_item", joinColumns = @JoinColumn(name = "summary_id"))
  @Column(name = "appraisal_id")
  private Set<UUID> appraisalIds = new HashSet<>();

  protected AppraisalSummary() {}

  /** Generated from at least one submitted appraisal. */
  public static AppraisalSummary generate(
      UUID apprenticeId, String period, UUID generatedBy, Set<UUID> submittedAppraisalIds) {
    if (submittedAppraisalIds.isEmpty()) {
      throw new InvalidAppraisalException("No submitted appraisal for this apprentice and period");
    }
    AppraisalSummary summary = new AppraisalSummary();
    summary.id = UUID.randomUUID();
    summary.apprenticeId = apprenticeId;
    summary.period = period;
    summary.generatedBy = generatedBy;
    summary.status = SummaryStatus.GENERATED;
    summary.appraisalIds.addAll(submittedAppraisalIds);
    return summary;
  }

  /** GENERATED to VALIDATED, only by the apprentice's own master (rule R9). */
  public void validate(UUID actorId, UUID masterOfApprentice) {
    if (!masterOfApprentice.equals(actorId)) {
      throw new ForbiddenActionException("Only the apprentice's master can validate the summary");
    }
    if (status != SummaryStatus.GENERATED) {
      throw new IllegalTransitionException("Cannot validate a summary in state " + status);
    }
    status = SummaryStatus.VALIDATED;
  }

  /** VALIDATED to SENT: the school can read the summary only from then on (hypothesis H-D01). */
  public void markSent() {
    if (status != SummaryStatus.VALIDATED) {
      throw new IllegalTransitionException("Only a validated summary can be sent");
    }
    status = SummaryStatus.SENT;
  }

  public UUID getId() {
    return id;
  }

  public UUID getApprenticeId() {
    return apprenticeId;
  }

  public String getPeriod() {
    return period;
  }

  public SummaryStatus getStatus() {
    return status;
  }

  public Set<UUID> getAppraisalIds() {
    return Set.copyOf(appraisalIds);
  }
}
