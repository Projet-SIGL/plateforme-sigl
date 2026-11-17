package com.sigl.projets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "project")
public class Project {

  @Id private UUID id;

  @Column(name = "manager_id", nullable = false)
  private UUID managerId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectStatus status;

  protected Project() {}

  public Project(UUID managerId, String name) {
    this.id = UUID.randomUUID();
    this.managerId = managerId;
    this.name = name;
    this.status = ProjectStatus.DRAFT;
  }

  /** A closed or archived project keeps its history: its teams can no longer change. */
  public boolean isFrozen() {
    return status == ProjectStatus.CLOSED || status == ProjectStatus.ARCHIVED;
  }

  public boolean isManagedBy(UUID userId) {
    return managerId.equals(userId);
  }

  public UUID getId() {
    return id;
  }

  public UUID getManagerId() {
    return managerId;
  }

  public String getName() {
    return name;
  }

  public ProjectStatus getStatus() {
    return status;
  }
}
