package com.sigl.apprentis.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.apprentis.application.CalendarService;
import com.sigl.apprentis.domain.CalendarPeriod;
import com.sigl.apprentis.domain.InvalidRequestException;
import com.sigl.apprentis.domain.PeriodType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CalendarController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class CalendarControllerTest {

  @Autowired MockMvc mvc;
  @MockBean CalendarService service;
  @MockBean JwtDecoder jwtDecoder;

  private final UUID apprentice = UUID.randomUUID();

  @Test
  void onlyHrAndAdminAddPeriods() throws Exception {
    mvc.perform(
            post("/api/apprentis/{id}/calendrier/periodes", apprentice)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CLASS\",\"start\":\"2026-09-01\",\"end\":\"2026-09-12\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MASTER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void hrAddsAPeriod() throws Exception {
    when(service.addPresencePeriod(any(), any(), any(), any()))
        .thenReturn(new CalendarPeriod(apprentice, PeriodType.CLASS, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 12)));

    mvc.perform(
            post("/api/apprentis/{id}/calendrier/periodes", apprentice)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"CLASS\",\"start\":\"2026-09-01\",\"end\":\"2026-09-12\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("CLASS"));
  }

  @Test
  void hrImportsACsv() throws Exception {
    when(service.importCsv(any(), any())).thenReturn(List.of());

    mvc.perform(
            post("/api/apprentis/{id}/calendrier/import", apprentice)
                .contentType("text/csv")
                .content("CLASS,2026-09-01,2026-09-12")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
        .andExpect(status().isCreated());
  }

  @Test
  void invalidFileIsABadRequest() throws Exception {
    when(service.importCsv(any(), any())).thenThrow(new InvalidRequestException("Line 2: invalid type or date"));

    mvc.perform(
            post("/api/apprentis/{id}/calendrier/import", apprentice)
                .contentType("text/csv")
                .content("bad")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }
}
