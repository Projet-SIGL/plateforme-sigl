package com.sigl.apprentis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** A dated period of the work-study calendar of an apprentice. */
@Entity
@Table(name = "calendar_period")
public class CalendarPeriod {

  @Id private UUID id;

  @Column(name = "apprentice_id", nullable = false)
  private UUID apprenticeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PeriodType type;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  protected CalendarPeriod() {}

  public CalendarPeriod(UUID apprenticeId, PeriodType type, LocalDate startDate, LocalDate endDate) {
    this.id = UUID.randomUUID();
    this.apprenticeId = apprenticeId;
    this.type = type;
    reschedule(startDate, endDate);
  }

  public void reschedule(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
      throw new InvalidRequestException("The period must have a start date not after its end date");
    }
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public boolean overlaps(CalendarPeriod other) {
    return !startDate.isAfter(other.endDate) && !other.startDate.isAfter(endDate);
  }

  public UUID getId() {
    return id;
  }

  public UUID getApprenticeId() {
    return apprenticeId;
  }

  public PeriodType getType() {
    return type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }
}
