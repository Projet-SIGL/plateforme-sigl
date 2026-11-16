package com.sigl.appreciations.application;

import java.util.UUID;

/** Rule R3: only someone who really supervised the apprentice on the project can appraise. */
public interface SupervisionPort {
  boolean hasSupervised(UUID evaluatorId, UUID apprenticeId, UUID projectId);
}
