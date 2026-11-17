package com.sigl.appreciations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sigl.appreciations.application.ApprenticeReferencePort.References;
import com.sigl.appreciations.application.SummaryService.Reader;
import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import com.sigl.appreciations.domain.AppraisalSummary;
import com.sigl.appreciations.domain.Criterion;
import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.InvalidAppraisalException;
import com.sigl.appreciations.domain.SummaryStatus;
import com.sigl.appreciations.infrastructure.AppraisalRepository;
import com.sigl.appreciations.infrastructure.SummaryRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SummaryServiceTest {

  @Mock SummaryRepository summaries;
  @Mock AppraisalRepository appraisals;
  @Mock ApprenticeReferencePort references;
  @Mock SummaryEventPublisher events;

  private SummaryService service;
  private final UUID hr = UUID.randomUUID();
  private final UUID master = UUID.randomUUID();
  private final UUID school = UUID.randomUUID();
  private final UUID apprentice = UUID.randomUUID();
  private final Criterion criterion = new Criterion(UUID.randomUUID(), "Teamwork", 5);

  @BeforeEach
  void setUp() {
    service = new SummaryService(summaries, appraisals, references, events);
    when(references.referencesOf(apprentice)).thenReturn(new References(master, school));
    when(summaries.save(any(AppraisalSummary.class))).thenAnswer(i -> i.getArgument(0));
  }

  private Appraisal submittedAppraisal() {
    UUID author = UUID.randomUUID();
    Appraisal appraisal = Appraisal.draft(apprentice, UUID.randomUUID(), author, "2030-S1");
    appraisal.replaceScores(Map.of(criterion.getId(), 4));
    appraisal.submit(author, Map.of(criterion.getId(), criterion));
    return appraisal;
  }

  private AppraisalSummary stored(Appraisal appraisal) {
    AppraisalSummary summary =
        AppraisalSummary.generate(apprentice, "2030-S1", hr, Set.of(appraisal.getId()));
    when(summaries.findById(summary.getId())).thenReturn(Optional.of(summary));
    when(appraisals.findAllById(summary.getAppraisalIds())).thenReturn(List.of(appraisal));
    return summary;
  }

  @Test
  void generatesFromSubmittedAppraisalsAndPublishesAnEvent() {
    Appraisal appraisal = submittedAppraisal();
    when(appraisals.findByApprenticeIdAndPeriodAndStatus(
            apprentice, "2030-S1", AppraisalStatus.SUBMITTED))
        .thenReturn(List.of(appraisal));

    AppraisalSummary summary = service.generate(hr, apprentice, "2030-S1");

    assertThat(summary.getAppraisalIds()).containsExactly(appraisal.getId());
    verify(events).summaryGenerated(summary.getId(), apprentice);
  }

  @Test
  void refusesToGenerateWithoutSubmittedAppraisal() {
    when(appraisals.findByApprenticeIdAndPeriodAndStatus(
            apprentice, "2030-S1", AppraisalStatus.SUBMITTED))
        .thenReturn(List.of());

    assertThatThrownBy(() -> service.generate(hr, apprentice, "2030-S1"))
        .isInstanceOf(InvalidAppraisalException.class);
    verify(events, never()).summaryGenerated(any(), any());
  }

  @Test
  void masterValidationCascadesToTheAppraisals() {
    Appraisal appraisal = submittedAppraisal();
    AppraisalSummary summary = stored(appraisal);

    service.validate(master, summary.getId());

    assertThat(summary.getStatus()).isEqualTo(SummaryStatus.VALIDATED);
    assertThat(appraisal.getStatus()).isEqualTo(AppraisalStatus.VALIDATED);
  }

  @Test
  void anotherMasterCannotValidate() {
    AppraisalSummary summary = stored(submittedAppraisal());

    assertThatThrownBy(() -> service.validate(UUID.randomUUID(), summary.getId()))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void transmissionPublishesTheEventWithTheSchool() {
    Appraisal appraisal = submittedAppraisal();
    AppraisalSummary summary = stored(appraisal);
    service.validate(master, summary.getId());

    service.transmit(summary.getId());

    assertThat(summary.getStatus()).isEqualTo(SummaryStatus.SENT);
    verify(events).summaryTransmitted(summary.getId(), apprentice, school);
  }

  @Test
  void schoolReadsOnlyAfterTransmission() {
    Appraisal appraisal = submittedAppraisal();
    AppraisalSummary summary = stored(appraisal);
    Reader schoolReader = new Reader(UUID.randomUUID(), true, school);

    service.validate(master, summary.getId());
    assertThatThrownBy(() -> service.read(schoolReader, summary.getId()))
        .isInstanceOf(ForbiddenActionException.class);

    service.transmit(summary.getId());
    assertThat(service.read(schoolReader, summary.getId())).isSameAs(summary);
  }

  @Test
  void anotherSchoolNeverReads() {
    Appraisal appraisal = submittedAppraisal();
    AppraisalSummary summary = stored(appraisal);
    service.validate(master, summary.getId());
    service.transmit(summary.getId());

    assertThatThrownBy(
            () -> service.read(new Reader(UUID.randomUUID(), true, UUID.randomUUID()), summary.getId()))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void staffCannotReadBeforeValidationExceptTheMaster() {
    AppraisalSummary summary = stored(submittedAppraisal());

    assertThatThrownBy(() -> service.read(new Reader(hr, false, null), summary.getId()))
        .isInstanceOf(ForbiddenActionException.class);
    assertThat(service.read(new Reader(master, false, null), summary.getId())).isSameAs(summary);
  }
}
