package com.sigl.appreciations.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.appreciations.application.AppraisalService;
import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.ForbiddenActionException;
import com.sigl.appreciations.domain.IllegalTransitionException;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(AppraisalController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class AppraisalControllerTest {

  @Autowired MockMvc mvc;
  @MockBean AppraisalService service;
  @MockBean JwtDecoder jwtDecoder;

  private final UUID author = UUID.randomUUID();
  private final String body =
      "{\"apprenticeId\":\"%s\",\"projectId\":\"%s\",\"period\":\"2030-S1\"}"
          .formatted(UUID.randomUUID(), UUID.randomUUID());

  private RequestPostProcessor as(String role) {
    return jwt()
        .jwt(j -> j.subject(author.toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Test
  void requiresAToken() throws Exception {
    mvc.perform(post("/api/appreciations").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refusesAnApprenticeEvenWithAValidToken() throws Exception {
    mvc.perform(
            post("/api/appreciations")
                .with(as("APPRENTICE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void createsADraft() throws Exception {
    when(service.createDraft(any(), any()))
        .thenReturn(Appraisal.draft(UUID.randomUUID(), UUID.randomUUID(), author, "2030-S1"));

    mvc.perform(
            post("/api/appreciations")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  void answers403WhenTheRoleIsNotEnoughForR3() throws Exception {
    when(service.createDraft(any(), any())).thenThrow(new ForbiddenActionException("R3"));

    mvc.perform(
            post("/api/appreciations")
                .with(as("MASTER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsAnInvalidBody() throws Exception {
    mvc.perform(
            post("/api/appreciations")
                .with(as("MASTER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"period\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void answers409OnAForbiddenTransition() throws Exception {
    when(service.submit(any(), any())).thenThrow(new IllegalTransitionException("already submitted"));

    mvc.perform(post("/api/appreciations/" + UUID.randomUUID() + "/soumission").with(as("MASTER")))
        .andExpect(status().isConflict());
  }

  @Test
  void anApprenticeListsTheirAppraisals() throws Exception {
    when(service.listFor(any(), any(), org.mockito.ArgumentMatchers.eq(false)))
        .thenReturn(List.of(Appraisal.draft(author, UUID.randomUUID(), UUID.randomUUID(), "2030-S1")));

    mvc.perform(get("/api/appreciations").param("apprenti", author.toString()).with(as("APPRENTICE")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].period").value("2030-S1"));
  }

  @Test
  void aProjectManagerCannotListThroughThisRoute() throws Exception {
    mvc.perform(get("/api/appreciations").param("apprenti", author.toString()).with(as("PROJECT_MANAGER")))
        .andExpect(status().isForbidden());
  }
}
