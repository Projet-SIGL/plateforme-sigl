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

  /**
   * Applies a lifecycle action (state diagram, Part 1 §5). Closing needs no open task (hypothesis
   * H-D05) and reopening needs an administrator and a reason.
   */
  public void apply(ProjectAction action, boolean admin, String reason, boolean hasOpenWork) {
    switch (action) {
      case START -> move(ProjectStatus.DRAFT, ProjectStatus.IN_PROGRESS);
      case SUSPEND -> move(ProjectStatus.IN_PROGRESS, ProjectStatus.SUSPENDED);
      case RESUME -> move(ProjectStatus.SUSPENDED, ProjectStatus.IN_PROGRESS);
      case CLOSE -> {
        requireState(ProjectStatus.IN_PROGRESS, "close");
        if (hasOpenWork) {
          throw new IllegalTransitionException("The project still has open tasks");
        }
        status = ProjectStatus.CLOSED;
      }
      case REOPEN -> {
        requireState(ProjectStatus.CLOSED, "reopen");
        if (!admin) {
          throw new ForbiddenActionException("Only an administrator reopens a closed project");
        }
        if (reason == null || reason.isBlank()) {
          throw new InvalidRequestException("Reopening a project needs a reason");
        }
        status = ProjectStatus.IN_PROGRESS;
      }
      case ARCHIVE -> {
        if (status != ProjectStatus.CLOSED && status != ProjectStatus.DRAFT) {
          throw new IllegalTransitionException("Cannot archive a project in state " + status);
        }
        status = ProjectStatus.ARCHIVED;
      }
    }
  }

  private void move(ProjectStatus from, ProjectStatus to) {
    requireState(from, "move");
    status = to;
  }

  private void requireState(ProjectStatus expected, String what) {
    if (status != expected) {
      throw new IllegalTransitionException("Cannot " + what + " a project in state " + status);
    }
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
