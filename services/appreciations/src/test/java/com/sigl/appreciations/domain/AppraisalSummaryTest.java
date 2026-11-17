package com.sigl.appreciations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppraisalSummaryTest {

  private final UUID master = UUID.randomUUID();
  private final UUID hr = UUID.randomUUID();

  private AppraisalSummary generated() {
    return AppraisalSummary.generate(UUID.randomUUID(), "2030-S1", hr, Set.of(UUID.randomUUID()));
  }

  @Test
  void needsAtLeastOneSubmittedAppraisal() {
    assertThatThrownBy(() -> AppraisalSummary.generate(UUID.randomUUID(), "2030-S1", hr, Set.of()))
        .isInstanceOf(InvalidAppraisalException.class);
  }

  @Test
  void followsGeneratedValidatedSent() {
    AppraisalSummary summary = generated();
    assertThat(summary.getStatus()).isEqualTo(SummaryStatus.GENERATED);
    summary.validate(master, master);
    assertThat(summary.getStatus()).isEqualTo(SummaryStatus.VALIDATED);
    summary.markSent();
    assertThat(summary.getStatus()).isEqualTo(SummaryStatus.SENT);
  }

  @Test
  void onlyTheApprenticesMasterValidates() {
    assertThatThrownBy(() -> generated().validate(UUID.randomUUID(), master))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void cannotBeSentBeforeValidation() {
    assertThatThrownBy(() -> generated().markSent()).isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void cannotBeValidatedTwice() {
    AppraisalSummary summary = generated();
    summary.validate(master, master);
    assertThatThrownBy(() -> summary.validate(master, master))
        .isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void cannotBeSentTwice() {
    AppraisalSummary summary = generated();
    summary.validate(master, master);
    summary.markSent();
    assertThatThrownBy(summary::markSent).isInstanceOf(IllegalTransitionException.class);
  }
}
