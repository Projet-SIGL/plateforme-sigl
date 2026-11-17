package com.sigl.apprentis.domain;

public class IllegalTransitionException extends RuntimeException {
  public IllegalTransitionException(String message) {
    super(message);
  }
}
