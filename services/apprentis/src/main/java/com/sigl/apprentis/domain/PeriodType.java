package com.sigl.apprentis.domain;

public enum PeriodType {
  CLASS,
  COMPANY,
  EXAM;

  /** Class and company periods describe where the apprentice is each week and cannot overlap. */
  public boolean isPresence() {
    return this != EXAM;
  }
}
