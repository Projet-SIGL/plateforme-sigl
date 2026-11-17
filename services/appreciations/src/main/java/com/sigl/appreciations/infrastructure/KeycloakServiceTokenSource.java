package com.sigl.appreciations.infrastructure;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
class KeycloakServiceTokenSource implements ServiceTokenSource {

  private final RestClient client = RestClient.create();
  private final String tokenUri;
  private final String clientId;
  private final String clientSecret;
  private String cached;
  private Instant expiresAt = Instant.EPOCH;

  KeycloakServiceTokenSource(
      @Value("${sigl.service-account.token-uri}") String tokenUri,
      @Value("${sigl.service-account.client-id}") String clientId,
      @Value("${sigl.service-account.client-secret}") String clientSecret) {
    this.tokenUri = tokenUri;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  @Override
  public synchronized String token() {
    if (Instant.now().isBefore(expiresAt)) {
      return cached;
    }
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "client_credentials");
    form.add("client_id", clientId);
    form.add("client_secret", clientSecret);
    @SuppressWarnings("unchecked")
    Map<String, Object> response =
        client
            .post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map.class);
    cached = (String) response.get("access_token");
    int seconds = ((Number) response.getOrDefault("expires_in", 60)).intValue();
    expiresAt = Instant.now().plusSeconds(Math.max(seconds - 10, 1));
    return cached;
  }
}
