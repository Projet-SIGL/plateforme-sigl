package com.sigl.apprentis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sigl.apprentis.application.ApprenticeService.Viewer;
import com.sigl.apprentis.domain.ForbiddenActionException;
import com.sigl.apprentis.domain.InvalidRequestException;
import com.sigl.apprentis.domain.NotFoundException;
import com.sigl.apprentis.domain.SchoolContact;
import com.sigl.apprentis.infrastructure.SchoolContactRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchoolContactServiceTest {

  @Mock SchoolContactRepository contacts;

  private final UUID school = UUID.randomUUID();
  private SchoolContact contact;
  private SchoolContactService service;

  @BeforeEach
  void setUp() {
    contact = new SchoolContact(school, "Old", "old@school.fr", null);
    service = new SchoolContactService(contacts);
  }

  @Test
  void hrUpdatesAnyContact() {
    when(contacts.findById(contact.getId())).thenReturn(Optional.of(contact));
    when(contacts.save(any())).thenAnswer(i -> i.getArgument(0));

    SchoolContact result =
        service.update(new Viewer(false, null), school, contact.getId(), "New", "new@school.fr", "0102");

    assertThat(result.getName()).isEqualTo("New");
    assertThat(result.getPhone()).isEqualTo("0102");
  }

  @Test
  void aContactOfAnotherSchoolIsRefused() {
    UUID other = UUID.randomUUID();
    SchoolContact foreign = new SchoolContact(other, "X", "x@other.fr", null);
    when(contacts.findById(foreign.getId())).thenReturn(Optional.of(foreign));

    assertThatThrownBy(
            () -> service.update(new Viewer(true, school), other, foreign.getId(), "X", "x@other.fr", null))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void unknownContactIsNotFound() {
    UUID id = UUID.randomUUID();
    when(contacts.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(new Viewer(false, null), school, id, "A", "a@b.fr", null))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void invalidEmailIsRejected() {
    when(contacts.findById(contact.getId())).thenReturn(Optional.of(contact));

    assertThatThrownBy(
            () -> service.update(new Viewer(false, null), school, contact.getId(), "A", "not-an-email", null))
        .isInstanceOf(InvalidRequestException.class);
  }
}
