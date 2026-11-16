package com.sigl.appreciations.domain;

public class ForbiddenActionException extends RuntimeException {
  public ForbiddenActionException(String message) {
    super(message);
  }
}
