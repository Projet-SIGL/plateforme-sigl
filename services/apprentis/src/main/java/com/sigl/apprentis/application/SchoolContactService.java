package com.sigl.apprentis.application;

import com.sigl.apprentis.application.ApprenticeService.Viewer;
import com.sigl.apprentis.domain.ForbiddenActionException;
import com.sigl.apprentis.domain.NotFoundException;
import com.sigl.apprentis.domain.SchoolContact;
import com.sigl.apprentis.infrastructure.SchoolContactRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolContactService {

  private final SchoolContactRepository contacts;

  public SchoolContactService(SchoolContactRepository contacts) {
    this.contacts = contacts;
  }

  /** HR and admins edit any contact; a school contact only edits contacts of its own school. */
  @Transactional
  public SchoolContact update(
      Viewer viewer, UUID schoolId, UUID contactId, String name, String email, String phone) {
    SchoolContact contact =
        contacts
            .findById(contactId)
            .filter(c -> c.getSchoolId().equals(schoolId))
            .orElseThrow(() -> new NotFoundException("Contact not found"));
    if (viewer.school() && !schoolId.equals(viewer.schoolId())) {
      throw new ForbiddenActionException("A school contact can only edit its own school");
    }
    contact.update(name, email, phone);
    return contacts.save(contact);
  }
}
