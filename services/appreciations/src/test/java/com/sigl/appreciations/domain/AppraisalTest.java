package com.sigl.appreciations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppraisalTest {

  private final UUID author = UUID.randomUUID();
  private final Criterion teamwork = new Criterion(UUID.randomUUID(), "Teamwork", 5);
  private final Criterion rigor = new Criterion(UUID.randomUUID(), "Rigor", 5);
  private final Map<UUID, Criterion> grid =
      Map.of(teamwork.getId(), teamwork, rigor.getId(), rigor);
  private Appraisal appraisal;

  @BeforeEach
  void setUp() {
    appraisal = Appraisal.draft(UUID.randomUUID(), UUID.randomUUID(), author, "2030-S1");
  }

  @Test
  void startsAsDraft() {
    assertThat(appraisal.getStatus()).isEqualTo(AppraisalStatus.DRAFT);
  }

  @Test
  void submitsACompleteGrid() {
    appraisal.replaceScores(Map.of(teamwork.getId(), 4, rigor.getId(), 3));
    appraisal.submit(author, grid);
    assertThat(appraisal.getStatus()).isEqualTo(AppraisalStatus.SUBMITTED);
  }

  @Test
  void rejectsAnIncompleteGrid() {
    appraisal.replaceScores(Map.of(teamwork.getId(), 4));
    assertThatThrownBy(() -> appraisal.submit(author, grid))
        .isInstanceOf(InvalidAppraisalException.class);
    assertThat(appraisal.getStatus()).isEqualTo(AppraisalStatus.DRAFT);
  }

  @Test
  void rejectsAScoreAboveTheMaximum() {
    appraisal.replaceScores(Map.of(teamwork.getId(), 6, rigor.getId(), 3));
    assertThatThrownBy(() -> appraisal.submit(author, grid))
        .isInstanceOf(InvalidAppraisalException.class);
  }

  @Test
  void rejectsAnotherAuthor() {
    appraisal.replaceScores(Map.of(teamwork.getId(), 4, rigor.getId(), 3));
    assertThatThrownBy(() -> appraisal.submit(UUID.randomUUID(), grid))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void rejectsASecondSubmissionAndFrozenScores() {
    appraisal.replaceScores(Map.of(teamwork.getId(), 4, rigor.getId(), 3));
    appraisal.submit(author, grid);
    assertThatThrownBy(() -> appraisal.submit(author, grid))
        .isInstanceOf(IllegalTransitionException.class);
    assertThatThrownBy(() -> appraisal.replaceScores(Map.of()))
        .isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void rejectsAnEmptyGrid() {
    assertThatThrownBy(() -> appraisal.submit(author, Map.of()))
        .isInstanceOf(InvalidAppraisalException.class);
  }

  @Test
  void validatesOnlyASubmittedAppraisal() {
    assertThatThrownBy(() -> appraisal.validate()).isInstanceOf(IllegalTransitionException.class);
    appraisal.replaceScores(Map.of(teamwork.getId(), 4, rigor.getId(), 3));
    appraisal.submit(author, grid);
    appraisal.validate();
    assertThat(appraisal.getStatus()).isEqualTo(AppraisalStatus.VALIDATED);
  }
}
