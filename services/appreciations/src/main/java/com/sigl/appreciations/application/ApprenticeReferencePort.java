package com.sigl.appreciations.application;

import java.util.UUID;

/** References of an apprentice, owned by the Apprentis service (/internal/apprentis/{id}/references). */
public interface ApprenticeReferencePort {

  record References(UUID masterId, UUID schoolId) {}

  References referencesOf(UUID apprenticeId);
}
