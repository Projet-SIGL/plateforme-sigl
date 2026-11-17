package com.sigl.appreciations.infrastructure;

import com.sigl.appreciations.application.SummaryEventPublisher;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Temporary adapter: the events are logged until the broker (RabbitMQ locally, Service Bus on Azure)
 * is wired by its own issue. Replace this class, not the port.
 */
@Component
class LoggingSummaryEventPublisher implements SummaryEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(LoggingSummaryEventPublisher.class);

  @Override
  public void summaryGenerated(UUID summaryId, UUID apprenticeId) {
    log.info("event appreciation.summary.generated summary={} apprentice={}", summaryId, apprenticeId);
  }

  @Override
  public void summaryTransmitted(UUID summaryId, UUID apprenticeId, UUID schoolId) {
    log.info(
        "event appreciation.summary.transmitted summary={} apprentice={} school={}",
        summaryId,
        apprenticeId,
        schoolId);
  }
}
