package com.sigl.appreciations.infrastructure;

/** Provides the bearer token of this service's own Keycloak account (client credentials). */
public interface ServiceTokenSource {
  String token();
}
