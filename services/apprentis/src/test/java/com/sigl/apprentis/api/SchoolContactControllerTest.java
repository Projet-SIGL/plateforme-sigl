package com.sigl.apprentis.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sigl.apprentis.application.SchoolContactService;
import com.sigl.apprentis.domain.InvalidRequestException;
import com.sigl.apprentis.domain.SchoolContact;
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

@WebMvcTest(SchoolContactController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class SchoolContactControllerTest {

  private static final String BODY = "{\"name\":\"New\",\"email\":\"new@school.fr\",\"phone\":\"0102\"}";

  @Autowired MockMvc mvc;
  @MockBean SchoolContactService service;
  @MockBean JwtDecoder jwtDecoder;

  private final UUID school = UUID.randomUUID();
  private final UUID contact = UUID.randomUUID();

  @Test
  void anApprenticeIsForbidden() throws Exception {
    mvc.perform(
            put("/api/ecoles/{s}/contacts/{c}", school, contact)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_APPRENTICE"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void hrUpdatesAContact() throws Exception {
    when(service.update(any(), any(), any(), any(), any(), any()))
        .thenReturn(new SchoolContact(school, "New", "new@school.fr", "0102"));

    mvc.perform(
            put("/api/ecoles/{s}/contacts/{c}", school, contact)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("new@school.fr"));
  }

  @Test
  void invalidDataIsABadRequest() throws Exception {
    when(service.update(any(), any(), any(), any(), any(), any()))
        .thenThrow(new InvalidRequestException("The contact email is invalid"));

    mvc.perform(
            put("/api/ecoles/{s}/contacts/{c}", school, contact)
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());
  }
}
