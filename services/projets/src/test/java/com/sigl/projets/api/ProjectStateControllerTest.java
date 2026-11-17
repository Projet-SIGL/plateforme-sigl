package com.sigl.projets.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.projets.application.ProjectLifecycleService;
import com.sigl.projets.domain.IllegalTransitionException;
import com.sigl.projets.domain.Project;
import com.sigl.projets.domain.ProjectAction;
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

@WebMvcTest(ProjectStateController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class ProjectStateControllerTest {

  @Autowired MockMvc mvc;
  @MockBean ProjectLifecycleService service;
  @MockBean JwtDecoder jwtDecoder;

  private RequestPostProcessor as(String role) {
    return jwt()
        .jwt(j -> j.subject(UUID.randomUUID().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Test
  void returnsTheStateReturnedByTheService() throws Exception {
    Project project = new Project(UUID.randomUUID(), "Platform");
    project.apply(ProjectAction.START, false, null, false);
    when(service.apply(any(), any(), any(), any())).thenReturn(project);

    mvc.perform(
            patch("/api/projets/" + project.getId() + "/etat")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"CLOSE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  void anApprenticeCannotChangeTheState() throws Exception {
    mvc.perform(
            patch("/api/projets/" + UUID.randomUUID() + "/etat")
                .with(as("APPRENTICE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"CLOSE\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void aForbiddenTransitionAnswers409() throws Exception {
    when(service.apply(any(), any(), any(), any())).thenThrow(new IllegalTransitionException("open tasks"));

    mvc.perform(
            patch("/api/projets/" + UUID.randomUUID() + "/etat")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"CLOSE\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void rejectsAnUnknownAction() throws Exception {
    mvc.perform(
            patch("/api/projets/" + UUID.randomUUID() + "/etat")
                .with(as("PROJECT_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"DELETE\"}"))
        .andExpect(status().isBadRequest());
  }
}
