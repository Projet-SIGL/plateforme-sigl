package com.sigl.apprentis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.sigl.apprentis.application.ApprenticeService.Viewer;
import com.sigl.apprentis.domain.Apprentice;
import com.sigl.apprentis.domain.ForbiddenActionException;
import com.sigl.apprentis.infrastructure.ApprenticeRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApprenticeServiceTest {

  @Mock ApprenticeRepository apprentices;

  private final UUID school = UUID.randomUUID();

  @Test
  void aSchoolContactOnlySearchesItsOwnSchool() {
    Apprentice mine = new Apprentice("Alice", school, null);
    when(apprentices.findByArchivedFalseAndSchoolIdAndNameContainingIgnoreCase(school, "al"))
        .thenReturn(List.of(mine));

    assertThat(new ApprenticeService(apprentices).search(new Viewer(true, school), " al "))
        .containsExactly(mine);
  }

  @Test
  void aSchoolContactWithoutSchoolIsRefused() {
    assertThatThrownBy(() -> new ApprenticeService(apprentices).search(new Viewer(true, null), null))
        .isInstanceOf(ForbiddenActionException.class);
  }

  @Test
  void staffSearchesEveryActiveApprentice() {
    Apprentice any = new Apprentice("Bob", UUID.randomUUID(), null);
    when(apprentices.findByArchivedFalseAndNameContainingIgnoreCase("")).thenReturn(List.of(any));

    assertThat(new ApprenticeService(apprentices).search(new Viewer(false, null), null))
        .containsExactly(any);
  }
}
