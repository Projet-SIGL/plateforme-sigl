package com.sigl.appreciations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.sigl.appreciations.application.ApprenticeReferencePort.References;
import com.sigl.appreciations.domain.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestApprenticeReferenceAdapterTest {

  private MockRestServiceServer server;
  private RestApprenticeReferenceAdapter adapter;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    adapter = new RestApprenticeReferenceAdapter(builder, () -> "service-token", "http://apprentices");
  }

  @Test
  void readsTheReferencesWithTheServiceAccountToken() {
    UUID apprentice = UUID.randomUUID();
    UUID master = UUID.randomUUID();
    UUID school = UUID.randomUUID();
    server
        .expect(requestTo("http://apprentices/internal/apprentis/" + apprentice + "/references"))
        .andExpect(header("Authorization", "Bearer service-token"))
        .andRespond(
            withSuccess(
                "{\"masterId\":\"%s\",\"schoolId\":\"%s\"}".formatted(master, school),
                MediaType.APPLICATION_JSON));

    assertThat(adapter.referencesOf(apprentice)).isEqualTo(new References(master, school));
    server.verify();
  }

  @Test
  void mapsAnUnknownApprenticeToNotFound() {
    UUID apprentice = UUID.randomUUID();
    server
        .expect(requestTo("http://apprentices/internal/apprentis/" + apprentice + "/references"))
        .andRespond(withResourceNotFound());

    assertThatThrownBy(() -> adapter.referencesOf(apprentice)).isInstanceOf(NotFoundException.class);
  }
}
