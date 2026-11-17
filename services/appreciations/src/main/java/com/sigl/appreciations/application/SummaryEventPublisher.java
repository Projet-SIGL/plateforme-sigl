package com.sigl.appreciations.application;

import java.util.UUID;

/** Events published on the broker (topic exchange "sigl.events"). */
public interface SummaryEventPublisher {
  void summaryGenerated(UUID summaryId, UUID apprenticeId);

  void summaryTransmitted(UUID summaryId, UUID apprenticeId, UUID schoolId);
}
