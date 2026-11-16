package com.sigl.appreciations.domain;

public class IllegalTransitionException extends RuntimeException {
  public IllegalTransitionException(String message) {
    super(message);
  }
}
