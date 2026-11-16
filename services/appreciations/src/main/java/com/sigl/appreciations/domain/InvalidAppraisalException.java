package com.sigl.appreciations.domain;

public class InvalidAppraisalException extends RuntimeException {
  public InvalidAppraisalException(String message) {
    super(message);
  }
}
