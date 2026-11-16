package com.sigl.appreciations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sigl.appreciations.application.AppraisalService.NewAppraisal;
import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import com.sigl.appreciations.domain.Criterion;
import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.NotFoundException;
import com.sigl.appreciations.infrastructure.AppraisalRepository;
import com.sigl.appreciations.infrastructure.CriterionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
class AppraisalServiceTest {

  @Mock AppraisalRepository appraisals;
  @Mock CriterionRepository criteria;
  @Mock SupervisionPort supervision;

  private AppraisalService service;
  private final UUID author = UUID.randomUUID();
  private final UUID apprentice = UUID.randomUUID();
  private final UUID project = UUID.randomUUID();
  private final Criterion teamwork = new Criterion(UUID.randomUUID(), "Teamwork", 5);

  @BeforeEach
  void setUp() {
    service = new AppraisalService(appraisals, criteria, supervision);
    when(appraisals.save(any(Appraisal.class))).thenAnswer(i -> i.getArgument(0));
    when(criteria.findByActiveTrue()).thenReturn(List.of(teamwork));
  }

  @Test
  void createsADraftForARealSupervisor() {
    when(supervision.hasSupervised(author, apprentice, project)).thenReturn(true);

    Appraisal created =
        service.createDraft(
            author, new NewAppraisal(apprentice, project, "2030-S1", Map.of(teamwork.getId(), 4)));

    assertThat(created.getStatus()).isEqualTo(AppraisalStatus.DRAFT);
    assertThat(created.getEvaluatorId()).isEqualTo(author);
  }

  @Test
  void refusesARoleWithoutEffectiveSupervision() {
    when(supervision.hasSupervised(author, apprentice, project)).thenReturn(false);

    assertThatThrownBy(
            () -> service.createDraft(author, new NewAppraisal(apprentice, project, "2030-S1", null)))
        .isInstanceOf(ForbiddenActionException.class);
    verify(appraisals, never()).save(any());
  }

  @Test
  void submitsACompleteDraft() {
    when(supervision.hasSupervised(author, apprentice, project)).thenReturn(true);
    Appraisal draft = Appraisal.draft(apprentice, project, author, "2030-S1");
    draft.replaceScores(Map.of(teamwork.getId(), 4));
    when(appraisals.findById(draft.getId())).thenReturn(Optional.of(draft));

    assertThat(service.submit(author, draft.getId()).getStatus())
        .isEqualTo(AppraisalStatus.SUBMITTED);
  }

  @Test
  void refusesSubmissionByAnotherAuthor() {
    Appraisal draft = Appraisal.draft(apprentice, project, author, "2030-S1");
    when(appraisals.findById(draft.getId())).thenReturn(Optional.of(draft));

    assertThatThrownBy(() -> service.submit(UUID.randomUUID(), draft.getId()))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void refusesSubmissionWhenSupervisionIsGone() {
    when(supervision.hasSupervised(author, apprentice, project)).thenReturn(false);
    Appraisal draft = Appraisal.draft(apprentice, project, author, "2030-S1");
    when(appraisals.findById(draft.getId())).thenReturn(Optional.of(draft));

    assertThatThrownBy(() -> service.submit(author, draft.getId()))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void reportsAnUnknownAppraisal() {
    UUID unknown = UUID.randomUUID();
    when(appraisals.findById(unknown)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submit(author, unknown)).isInstanceOf(NotFoundException.class);
  }
}
