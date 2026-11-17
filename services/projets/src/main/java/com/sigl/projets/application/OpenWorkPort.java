package com.sigl.projets.application;

import java.util.UUID;

/** Tells whether a project still has tasks that are not DONE (closure guard). */
public interface OpenWorkPort {
  boolean hasOpenWork(UUID projectId);
}
