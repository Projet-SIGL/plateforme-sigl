package com.sigl.appreciations.infrastructure;

import com.sigl.appreciations.application.ApprenticeReferencePort;
import com.sigl.appreciations.domain.NotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/** Calls /internal/apprentis/{id}/references with the service account, never a user token. */
@Component
class RestApprenticeReferenceAdapter implements ApprenticeReferencePort {

  private final RestClient client;
  private final ServiceTokenSource tokens;

  RestApprenticeReferenceAdapter(
      RestClient.Builder builder,
      ServiceTokenSource tokens,
      @Value("${sigl.apprentices.base-url}") String baseUrl) {
    this.client = builder.baseUrl(baseUrl).build();
    this.tokens = tokens;
  }

  @Override
  public References referencesOf(UUID apprenticeId) {
    try {
      return client
          .get()
          .uri("/internal/apprentis/{id}/references", apprenticeId)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.token())
          .retrieve()
          .body(References.class);
    } catch (HttpClientErrorException.NotFound e) {
      throw new NotFoundException("Apprentice not found");
    }
  }
}
