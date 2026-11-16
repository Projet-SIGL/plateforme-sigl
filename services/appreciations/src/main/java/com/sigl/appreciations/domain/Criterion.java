package com.sigl.appreciations.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "criterion")
public class Criterion {

  @Id private UUID id;

  @Column(nullable = false)
  private String label;

  @Column(name = "max_score", nullable = false)
  private int maxScore;

  @Column(nullable = false)
  private boolean active;

  protected Criterion() {}

  public Criterion(UUID id, String label, int maxScore) {
    if (maxScore <= 0) {
      throw new InvalidAppraisalException("Criterion max score must be positive");
    }
    this.id = id;
    this.label = label;
    this.maxScore = maxScore;
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  public UUID getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public int getMaxScore() {
    return maxScore;
  }

  public boolean isActive() {
    return active;
  }
}
