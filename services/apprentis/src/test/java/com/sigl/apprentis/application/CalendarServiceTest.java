package com.sigl.apprentis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sigl.apprentis.domain.CalendarPeriod;
import com.sigl.apprentis.domain.InvalidRequestException;
import com.sigl.apprentis.domain.NotFoundException;
import com.sigl.apprentis.domain.PeriodType;
import com.sigl.apprentis.infrastructure.ApprenticeRepository;
import com.sigl.apprentis.infrastructure.CalendarPeriodRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

  @Mock CalendarPeriodRepository periods;
  @Mock ApprenticeRepository apprentices;

  private final UUID apprentice = UUID.randomUUID();
  private CalendarService service;

  @BeforeEach
  void setUp() {
    service = new CalendarService(periods, apprentices);
  }

  private void apprenticeExists() {
    when(apprentices.existsById(apprentice)).thenReturn(true);
  }

  @Test
  void unknownApprenticeIsNotFound() {
    when(apprentices.existsById(apprentice)).thenReturn(false);

    assertThatThrownBy(() -> service.list(apprentice)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void addsAClassPeriod() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());
    when(periods.save(any())).thenAnswer(i -> i.getArgument(0));

    CalendarPeriod saved =
        service.addPresencePeriod(
            apprentice, PeriodType.CLASS, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 12));

    assertThat(saved.getType()).isEqualTo(PeriodType.CLASS);
  }

  @Test
  void anExamCannotBeAddedAsPresencePeriod() {
    apprenticeExists();

    assertThatThrownBy(
            () ->
                service.addPresencePeriod(
                    apprentice, PeriodType.EXAM, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2)))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void startAfterEndIsRejected() {
    apprenticeExists();

    assertThatThrownBy(
            () ->
                service.addPresencePeriod(
                    apprentice, PeriodType.CLASS, LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 1)))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void overlappingPresencePeriodsAreRejected() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice))
        .thenReturn(
            List.of(new CalendarPeriod(apprentice, PeriodType.COMPANY, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))));

    assertThatThrownBy(
            () ->
                service.addPresencePeriod(
                    apprentice, PeriodType.CLASS, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 2)))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void importsAValidCsvWithHeader() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());
    when(periods.saveAll(any())).thenAnswer(i -> i.getArgument(0));

    List<CalendarPeriod> result =
        service.importCsv(
            apprentice, "type,start,end\nCLASS,2026-09-01,2026-09-12\ncompany,2026-09-13,2026-10-10\n");

    assertThat(result).hasSize(2);
  }

  @Test
  void aBadLineRejectsTheWholeFile() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());

    assertThatThrownBy(
            () -> service.importCsv(apprentice, "CLASS,2026-09-01,2026-09-12\nCLASS,not-a-date,2026-10-01"))
        .isInstanceOf(InvalidRequestException.class)
        .hasMessageContaining("Line 2");
    verify(periods, never()).saveAll(any());
  }

  @Test
  void anEmptyFileIsRejected() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());

    assertThatThrownBy(() -> service.importCsv(apprentice, "type,start,end\n"))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void overlapInsideTheFileIsRejected() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());

    assertThatThrownBy(
            () -> service.importCsv(apprentice, "CLASS,2026-09-01,2026-09-20\nCOMPANY,2026-09-10,2026-10-01"))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void anExamMayOverlapAClassPeriod() {
    apprenticeExists();
    when(periods.save(any())).thenAnswer(i -> i.getArgument(0));

    CalendarPeriod exam = service.addExam(apprentice, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 16));

    assertThat(exam.getType()).isEqualTo(PeriodType.EXAM);
  }

  @Test
  void updatesAnExam() {
    CalendarPeriod exam = new CalendarPeriod(apprentice, PeriodType.EXAM, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 16));
    when(periods.findById(exam.getId())).thenReturn(Optional.of(exam));
    when(periods.save(any())).thenAnswer(i -> i.getArgument(0));

    CalendarPeriod updated =
        service.updateExam(apprentice, exam.getId(), LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 22));

    assertThat(updated.getStartDate()).isEqualTo(LocalDate.of(2026, 9, 20));
  }

  @Test
  void aNonExamPeriodCannotBeUpdatedAsExam() {
    CalendarPeriod classPeriod = new CalendarPeriod(apprentice, PeriodType.CLASS, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5));
    when(periods.findById(classPeriod.getId())).thenReturn(Optional.of(classPeriod));

    assertThatThrownBy(
            () -> service.updateExam(apprentice, classPeriod.getId(), LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 3)))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void anExamOfAnotherApprenticeCannotBeUpdated() {
    CalendarPeriod exam = new CalendarPeriod(UUID.randomUUID(), PeriodType.EXAM, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 16));
    when(periods.findById(exam.getId())).thenReturn(Optional.of(exam));

    assertThatThrownBy(
            () -> service.updateExam(apprentice, exam.getId(), LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 22)))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void invertedExamDatesAreRejected() {
    apprenticeExists();

    assertThatThrownBy(() -> service.addExam(apprentice, LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 10)))
        .isInstanceOf(InvalidRequestException.class);
  }

  private static String ics(String... events) {
    return "BEGIN:VCALENDAR\r\n" + String.join("", events) + "END:VCALENDAR\r\n";
  }

  private static String event(String summary, String start, String end) {
    return "BEGIN:VEVENT\r\nSUMMARY:" + summary + "\r\nDTSTART;VALUE=DATE:" + start
        + "\r\nDTEND;VALUE=DATE:" + end + "\r\nEND:VEVENT\r\n";
  }

  @Test
  void importsIcalEventsWithExclusiveEnd() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());
    when(periods.saveAll(any())).thenAnswer(i -> i.getArgument(0));

    List<CalendarPeriod> result =
        service.importIcal(
            apprentice,
            ics(event("Cours à l'école", "20260901", "20260913"), event("Entreprise", "20260913", "20261011")));

    assertThat(result).extracting(CalendarPeriod::getType).containsExactly(PeriodType.CLASS, PeriodType.COMPANY);
    assertThat(result.get(0).getEndDate()).isEqualTo(LocalDate.of(2026, 9, 12));
  }

  @Test
  void anUnknownIcalEventIsRejected() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());

    assertThatThrownBy(() -> service.importIcal(apprentice, ics(event("Holidays", "20260901", "20260905"))))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void anIcalWithoutEventIsRejected() {
    apprenticeExists();
    when(periods.findByApprenticeIdOrderByStartDate(apprentice)).thenReturn(List.of());

    assertThatThrownBy(() -> service.importIcal(apprentice, ics()))
        .isInstanceOf(InvalidRequestException.class);
  }
}
