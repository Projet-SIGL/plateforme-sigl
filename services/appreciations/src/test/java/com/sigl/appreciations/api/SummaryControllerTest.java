package com.sigl.appreciations.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.appreciations.application.SummaryService;
import com.sigl.appreciations.domain.AppraisalSummary;
import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.IllegalTransitionException;
import java.util.Set;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(SummaryController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class SummaryControllerTest {

  @Autowired MockMvc mvc;
  @MockBean SummaryService service;
  @MockBean JwtDecoder jwtDecoder;

  private final UUID user = UUID.randomUUID();

  private RequestPostProcessor as(String role) {
    return jwt().jwt(j -> j.subject(user.toString())).authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  private AppraisalSummary summary() {
    return AppraisalSummary.generate(UUID.randomUUID(), "2030-S1", user, Set.of(UUID.randomUUID()));
  }

  @Test
  void hrGeneratesASummary() throws Exception {
    when(service.generate(any(), any(), any())).thenReturn(summary());

    mvc.perform(
            post("/api/syntheses")
                .with(as("HR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"apprenticeId\":\"%s\",\"period\":\"2030-S1\"}".formatted(UUID.randomUUID())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("GENERATED"));
  }

  @Test
  void aMasterCannotGenerate() throws Exception {
    mvc.perform(
            post("/api/syntheses")
                .with(as("MASTER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"apprenticeId\":\"%s\",\"period\":\"2030-S1\"}".formatted(UUID.randomUUID())))
        .andExpect(status().isForbidden());
  }

  @Test
  void onlyAMasterValidates() throws Exception {
    mvc.perform(post("/api/syntheses/" + UUID.randomUUID() + "/validation").with(as("HR")))
        .andExpect(status().isForbidden());
  }

  @Test
  void validationByTheWrongMasterIs403() throws Exception {
    when(service.validate(any(), any())).thenThrow(new ForbiddenActionException("not your apprentice"));

    mvc.perform(post("/api/syntheses/" + UUID.randomUUID() + "/validation").with(as("MASTER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void transmissionBeforeValidationIs409() throws Exception {
    when(service.transmit(any())).thenThrow(new IllegalTransitionException("not validated"));

    mvc.perform(post("/api/syntheses/" + UUID.randomUUID() + "/transmission").with(as("HR")))
        .andExpect(status().isConflict());
  }

  @Test
  void aSchoolContactReadsThroughTheService() throws Exception {
    when(service.read(any(), any())).thenReturn(summary());

    mvc.perform(
            get("/api/syntheses/" + UUID.randomUUID())
                .with(jwt().jwt(j -> j.subject(user.toString()).claim("school_id", UUID.randomUUID().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_SCHOOL"))))
        .andExpect(status().isOk());
  }

  @Test
  void anApprenticeCannotRead() throws Exception {
    mvc.perform(get("/api/syntheses/" + UUID.randomUUID()).with(as("APPRENTICE")))
        .andExpect(status().isForbidden());
  }
}
