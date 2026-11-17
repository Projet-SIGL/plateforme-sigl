package com.sigl.appreciations.application;

import com.sigl.appreciations.application.ApprenticeReferencePort.References;
import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import com.sigl.appreciations.domain.AppraisalSummary;
import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.NotFoundException;
import com.sigl.appreciations.domain.SummaryStatus;
import com.sigl.appreciations.infrastructure.AppraisalRepository;
import com.sigl.appreciations.infrastructure.SummaryRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummaryService {

  /** Who is reading: user id, whether the user is a school contact, and the school they belong to. */
  public record Reader(UUID userId, boolean school, UUID schoolId) {}

  private final SummaryRepository summaries;
  private final AppraisalRepository appraisals;
  private final ApprenticeReferencePort references;
  private final SummaryEventPublisher events;

  public SummaryService(
      SummaryRepository summaries,
      AppraisalRepository appraisals,
      ApprenticeReferencePort references,
      SummaryEventPublisher events) {
    this.summaries = summaries;
    this.appraisals = appraisals;
    this.references = references;
    this.events = events;
  }

  @Transactional
  public AppraisalSummary generate(UUID actorId, UUID apprenticeId, String period) {
    List<Appraisal> submitted =
        appraisals.findByApprenticeIdAndPeriodAndStatus(
            apprenticeId, period, AppraisalStatus.SUBMITTED);
    Set<UUID> ids = submitted.stream().map(Appraisal::getId).collect(Collectors.toSet());
    AppraisalSummary summary = AppraisalSummary.generate(apprenticeId, period, actorId, ids);
    summaries.save(summary);
    events.summaryGenerated(summary.getId(), apprenticeId);
    return summary;
  }

  /** Validation by the master also validates the appraisals it summarises (hypothesis H-D02). */
  @Transactional
  public AppraisalSummary validate(UUID actorId, UUID summaryId) {
    AppraisalSummary summary = load(summaryId);
    References refs = references.referencesOf(summary.getApprenticeId());
    summary.validate(actorId, refs.masterId());
    appraisals.findAllById(summary.getAppraisalIds()).forEach(Appraisal::validate);
    return summary;
  }

  @Transactional
  public AppraisalSummary transmit(UUID summaryId) {
    AppraisalSummary summary = load(summaryId);
    References refs = references.referencesOf(summary.getApprenticeId());
    summary.markSent();
    events.summaryTransmitted(summary.getId(), summary.getApprenticeId(), refs.schoolId());
    return summary;
  }

  /** Staff read at any time; a school contact only reads a SENT summary of its own apprentice. */
  @Transactional(readOnly = true)
  public AppraisalSummary read(Reader reader, UUID summaryId) {
    AppraisalSummary summary = load(summaryId);
    if (reader.school()) {
      boolean ownSchool =
          reader.schoolId() != null
              && reader.schoolId().equals(references.referencesOf(summary.getApprenticeId()).schoolId());
      if (summary.getStatus() != SummaryStatus.SENT || !ownSchool) {
        throw new ForbiddenActionException("The summary is not available to this school");
      }
    } else if (summary.getStatus() == SummaryStatus.GENERATED) {
      // Before the master's validation the sheet is not readable (specification, Part 2 §4).
      requireMaster(reader.userId(), summary);
    }
    return summary;
  }

  private void requireMaster(UUID userId, AppraisalSummary summary) {
    UUID master = references.referencesOf(summary.getApprenticeId()).masterId();
    if (!master.equals(userId)) {
      throw new ForbiddenActionException("The summary is not validated yet");
    }
  }

  private AppraisalSummary load(UUID id) {
    return summaries.findById(id).orElseThrow(() -> new NotFoundException("Summary not found"));
  }
}
