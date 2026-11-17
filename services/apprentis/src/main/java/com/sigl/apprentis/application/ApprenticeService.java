package com.sigl.apprentis.application;

import com.sigl.apprentis.domain.Apprentice;
import com.sigl.apprentis.domain.ForbiddenActionException;
import com.sigl.apprentis.infrastructure.ApprenticeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprenticeService {

  /** The caller: whether they are a school contact, and the school they belong to. */
  public record Viewer(boolean school, UUID schoolId) {}

  private final ApprenticeRepository apprentices;

  public ApprenticeService(ApprenticeRepository apprentices) {
    this.apprentices = apprentices;
  }

  /** A school contact only sees the apprentices of their own school (rule R07). */
  @Transactional(readOnly = true)
  public List<Apprentice> search(Viewer viewer, String text) {
    String query = text == null ? "" : text.trim();
    if (viewer.school()) {
      if (viewer.schoolId() == null) {
        throw new ForbiddenActionException("The school of this contact is unknown");
      }
      return apprentices.findByArchivedFalseAndSchoolIdAndNameContainingIgnoreCase(
          viewer.schoolId(), query);
    }
    return apprentices.findByArchivedFalseAndNameContainingIgnoreCase(query);
  }
}
