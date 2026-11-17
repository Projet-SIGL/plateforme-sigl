package com.sigl.apprentis.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.apprentis.application.ApprenticeService;
import com.sigl.apprentis.domain.Apprentice;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApprenticeController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class ApprenticeControllerTest {

  @Autowired MockMvc mvc;
  @MockBean ApprenticeService service;
  @MockBean JwtDecoder jwtDecoder;

  @Test
  void requiresAToken() throws Exception {
    mvc.perform(get("/api/apprentis")).andExpect(status().isUnauthorized());
  }

  @Test
  void anApprenticeCannotListTheFiles() throws Exception {
    mvc.perform(
            get("/api/apprentis")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_APPRENTICE"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void aSchoolContactListsThroughTheService() throws Exception {
    when(service.search(any(), any()))
        .thenReturn(List.of(new Apprentice("Alice", UUID.randomUUID(), null)));

    mvc.perform(
            get("/api/apprentis")
                .param("recherche", "al")
                .with(
                    jwt()
                        .jwt(j -> j.subject(UUID.randomUUID().toString()).claim("school_id", UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_SCHOOL"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Alice"));
  }

  @Test
  void hrListsWithoutSchool() throws Exception {
    when(service.search(any(), any())).thenReturn(List.of());

    mvc.perform(
            get("/api/apprentis")
                .with(jwt().jwt(j -> j.subject(UUID.randomUUID().toString())).authorities(new SimpleGrantedAuthority("ROLE_HR"))))
        .andExpect(status().isOk());
  }
}
