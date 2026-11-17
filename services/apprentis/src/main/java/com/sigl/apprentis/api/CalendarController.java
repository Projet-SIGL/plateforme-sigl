package com.sigl.apprentis.api;

import com.sigl.apprentis.application.CalendarService;
import com.sigl.apprentis.domain.CalendarPeriod;
import com.sigl.apprentis.domain.PeriodType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apprentis/{apprenticeId}/calendrier")
class CalendarController {

  record PeriodRequest(PeriodType type, LocalDate start, LocalDate end) {}

  record PeriodResponse(UUID id, PeriodType type, LocalDate start, LocalDate end) {
    static PeriodResponse of(CalendarPeriod p) {
      return new PeriodResponse(p.getId(), p.getType(), p.getStartDate(), p.getEndDate());
    }
  }

  private final CalendarService service;

  CalendarController(CalendarService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'MANAGEMENT', 'PROJECT_MANAGER', 'MASTER')")
  List<PeriodResponse> list(@PathVariable UUID apprenticeId) {
    return service.list(apprenticeId).stream().map(PeriodResponse::of).toList();
  }

  @PostMapping("/periodes")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
  PeriodResponse add(@PathVariable UUID apprenticeId, @RequestBody PeriodRequest body) {
    return PeriodResponse.of(
        service.addPresencePeriod(apprenticeId, body.type(), body.start(), body.end()));
  }

  @PostMapping(value = "/import", consumes = {"text/csv", "text/plain"})
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
  List<PeriodResponse> importCsv(@PathVariable UUID apprenticeId, @RequestBody String csv) {
    return service.importCsv(apprenticeId, csv).stream().map(PeriodResponse::of).toList();
  }
}
