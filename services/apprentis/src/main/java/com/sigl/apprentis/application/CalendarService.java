package com.sigl.apprentis.application;

import com.sigl.apprentis.domain.CalendarPeriod;
import com.sigl.apprentis.domain.InvalidRequestException;
import com.sigl.apprentis.domain.NotFoundException;
import com.sigl.apprentis.domain.PeriodType;
import com.sigl.apprentis.infrastructure.ApprenticeRepository;
import com.sigl.apprentis.infrastructure.CalendarPeriodRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarService {

  private final CalendarPeriodRepository periods;
  private final ApprenticeRepository apprentices;

  public CalendarService(CalendarPeriodRepository periods, ApprenticeRepository apprentices) {
    this.periods = periods;
    this.apprentices = apprentices;
  }

  @Transactional(readOnly = true)
  public List<CalendarPeriod> list(UUID apprenticeId) {
    requireApprentice(apprenticeId);
    return periods.findByApprenticeIdOrderByStartDate(apprenticeId);
  }

  /** Adds one class or company period (R05). */
  @Transactional
  public CalendarPeriod addPresencePeriod(
      UUID apprenticeId, PeriodType type, LocalDate start, LocalDate end) {
    requireApprentice(apprenticeId);
    if (type == null || !type.isPresence()) {
      throw new InvalidRequestException("Only CLASS and COMPANY periods can be set here");
    }
    CalendarPeriod period = new CalendarPeriod(apprenticeId, type, start, end);
    rejectOverlap(period, periods.findByApprenticeIdOrderByStartDate(apprenticeId));
    return periods.save(period);
  }

  /** Enters an exam period, which may overlap class or company periods (R06). */
  @Transactional
  public CalendarPeriod addExam(UUID apprenticeId, LocalDate start, LocalDate end) {
    requireApprentice(apprenticeId);
    return periods.save(new CalendarPeriod(apprenticeId, PeriodType.EXAM, start, end));
  }

  @Transactional
  public CalendarPeriod updateExam(UUID apprenticeId, UUID periodId, LocalDate start, LocalDate end) {
    CalendarPeriod exam =
        periods
            .findById(periodId)
            .filter(p -> p.getApprenticeId().equals(apprenticeId) && p.getType() == PeriodType.EXAM)
            .orElseThrow(() -> new NotFoundException("Exam period not found"));
    exam.reschedule(start, end);
    return periods.save(exam);
  }

  /**
   * Imports a CSV calendar ("CLASS|COMPANY,yyyy-MM-dd,yyyy-MM-dd" per line, optional header).
   * All-or-nothing: one invalid line rejects the whole file.
   */
  @Transactional
  public List<CalendarPeriod> importCsv(UUID apprenticeId, String csv) {
    requireApprentice(apprenticeId);
    List<CalendarPeriod> existing = new ArrayList<>(periods.findByApprenticeIdOrderByStartDate(apprenticeId));
    List<CalendarPeriod> imported = new ArrayList<>();
    String[] lines = csv == null ? new String[0] : csv.split("\\R");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();
      if (line.isEmpty() || (i == 0 && line.toLowerCase().startsWith("type"))) {
        continue;
      }
      CalendarPeriod period = parse(apprenticeId, line, i + 1);
      rejectOverlap(period, existing);
      existing.add(period);
      imported.add(period);
    }
    if (imported.isEmpty()) {
      throw new InvalidRequestException("The file contains no period");
    }
    return periods.saveAll(imported);
  }

  private CalendarPeriod parse(UUID apprenticeId, String line, int number) {
    String[] cells = line.split("[,;]");
    if (cells.length != 3) {
      throw new InvalidRequestException("Line " + number + ": expected type,start,end");
    }
    try {
      PeriodType type = PeriodType.valueOf(cells[0].trim().toUpperCase());
      if (!type.isPresence()) {
        throw new InvalidRequestException("Line " + number + ": only CLASS and COMPANY are accepted");
      }
      return new CalendarPeriod(
          apprenticeId, type, LocalDate.parse(cells[1].trim()), LocalDate.parse(cells[2].trim()));
    } catch (IllegalArgumentException | DateTimeParseException e) {
      throw new InvalidRequestException("Line " + number + ": invalid type or date");
    }
  }

  private void rejectOverlap(CalendarPeriod candidate, List<CalendarPeriod> existing) {
    boolean clash =
        existing.stream().filter(p -> p.getType().isPresence()).anyMatch(candidate::overlaps);
    if (clash) {
      throw new InvalidRequestException("The period overlaps an existing class or company period");
    }
  }

  private void requireApprentice(UUID apprenticeId) {
    if (!apprentices.existsById(apprenticeId)) {
      throw new NotFoundException("Apprentice not found");
    }
  }
}
