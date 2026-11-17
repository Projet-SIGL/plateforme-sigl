package com.sigl.projets.domain;

public class IllegalTransitionException extends RuntimeException {
  public IllegalTransitionException(String message) {
    super(message);
  }
}
