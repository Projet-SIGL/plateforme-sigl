package com.sigl.projets.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjectTest {

  private Project inProgress() {
    Project project = new Project(UUID.randomUUID(), "Platform");
    project.apply(ProjectAction.START, false, null, false);
    return project;
  }

  private Project closed() {
    Project project = inProgress();
    project.apply(ProjectAction.CLOSE, false, null, false);
    return project;
  }

  @Test
  void followsTheNominalLifecycle() {
    Project project = closed();
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.CLOSED);
    project.apply(ProjectAction.ARCHIVE, false, null, false);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
  }

  @Test
  void suspendsAndResumes() {
    Project project = inProgress();
    project.apply(ProjectAction.SUSPEND, false, null, false);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.SUSPENDED);
    project.apply(ProjectAction.RESUME, false, null, false);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
  }

  @Test
  void cannotCloseWithOpenWork() {
    assertThatThrownBy(() -> inProgress().apply(ProjectAction.CLOSE, false, null, true))
        .isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void cannotCloseAProjectThatIsNotInProgress() {
    assertThatThrownBy(() -> new Project(UUID.randomUUID(), "P").apply(ProjectAction.CLOSE, false, null, false))
        .isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void cannotArchiveAProjectInProgress() {
    assertThatThrownBy(() -> inProgress().apply(ProjectAction.ARCHIVE, false, null, false))
        .isInstanceOf(IllegalTransitionException.class);
  }

  @Test
  void anArchivedProjectIsFinal() {
    Project project = closed();
    project.apply(ProjectAction.ARCHIVE, true, null, false);
    for (ProjectAction action : ProjectAction.values()) {
      assertThatThrownBy(() -> project.apply(action, true, "reason", false))
          .isInstanceOf(IllegalTransitionException.class);
    }
  }

  @Test
  void onlyAnAdminReopensWithAReason() {
    Project project = closed();
    assertThatThrownBy(() -> project.apply(ProjectAction.REOPEN, false, "mistake", false))
        .isInstanceOf(ForbiddenActionException.class);
    assertThatThrownBy(() -> project.apply(ProjectAction.REOPEN, true, " ", false))
        .isInstanceOf(InvalidRequestException.class);
    project.apply(ProjectAction.REOPEN, true, "closed by mistake", false);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
  }
}
