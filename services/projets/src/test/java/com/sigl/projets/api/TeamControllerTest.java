package com.sigl.projets.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.projets.application.TeamService;
import com.sigl.projets.domain.ForbiddenActionException;
import com.sigl.projets.domain.IllegalTransitionException;
import com.sigl.projets.domain.Team;
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

@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class TeamControllerTest {

  @Autowired MockMvc mvc;
  @MockBean TeamService service;
  @MockBean JwtDecoder jwtDecoder;

  private final UUID project = UUID.randomUUID();

  private RequestPostProcessor as(String role) {
    return jwt()
        .jwt(j -> j.subject(UUID.randomUUID().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Test
  void requiresAToken() throws Exception {
    mvc.perform(post("/api/projets/" + project + "/equipes").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"QA\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void anApprenticeCannotCreateATeam() throws Exception {
    mvc.perform(
            post("/api/projets/" + project + "/equipes")
                .with(as("APPRENTICE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"QA\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void aManagerCreatesATeam() throws Exception {
    when(service.create(any(), any(), any())).thenReturn(new Team(project, "QA"));

    mvc.perform(
            post("/api/projets/" + project + "/equipes")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"QA\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("QA"));
  }

  @Test
  void rejectsABlankName() throws Exception {
    mvc.perform(
            post("/api/projets/" + project + "/equipes")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aManagerOfAnotherProjectGets403() throws Exception {
    when(service.create(any(), any(), any())).thenThrow(new ForbiddenActionException("not yours"));

    mvc.perform(
            post("/api/projets/" + project + "/equipes")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"QA\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void aClosedProjectAnswers409() throws Exception {
    when(service.replaceMembers(any(), any(), any(), any())).thenThrow(new IllegalTransitionException("closed"));

    mvc.perform(
            put("/api/projets/" + project + "/equipes/" + UUID.randomUUID() + "/membres")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"members\":[]}"))
        .andExpect(status().isConflict());
  }

  @Test
  void membersAreRequired() throws Exception {
    mvc.perform(
            put("/api/projets/" + project + "/equipes/" + UUID.randomUUID() + "/membres")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }
}
