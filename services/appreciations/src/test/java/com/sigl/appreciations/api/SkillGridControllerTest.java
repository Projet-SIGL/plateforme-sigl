package com.sigl.appreciations.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.appreciations.application.SkillGridService;
import com.sigl.appreciations.domain.Criterion;
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

@WebMvcTest(SkillGridController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class SkillGridControllerTest {

  @Autowired MockMvc mvc;
  @MockBean SkillGridService grid;
  @MockBean JwtDecoder jwtDecoder;

  private static final String BODY = "{\"criteria\":[{\"label\":\"Teamwork\",\"maxScore\":5}]}";

  @Test
  void anyAuthenticatedUserReadsTheGrid() throws Exception {
    when(grid.activeGrid()).thenReturn(List.of(new Criterion(UUID.randomUUID(), "Teamwork", 5)));

    mvc.perform(get("/api/grilles").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].label").value("Teamwork"));
  }

  @Test
  void onlyAnAdminReplacesTheGrid() throws Exception {
    mvc.perform(
            put("/api/grilles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isForbidden());
  }

  @Test
  void anAdminReplacesTheGrid() throws Exception {
    when(grid.replaceGrid(any())).thenReturn(List.of(new Criterion(UUID.randomUUID(), "Teamwork", 5)));

    mvc.perform(
            put("/api/grilles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsAnEmptyGrid() throws Exception {
    mvc.perform(
            put("/api/grilles")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"criteria\":[]}"))
        .andExpect(status().isBadRequest());
  }
}
