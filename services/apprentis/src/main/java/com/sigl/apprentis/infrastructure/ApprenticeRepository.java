package com.sigl.apprentis.infrastructure;

import com.sigl.apprentis.domain.Apprentice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprenticeRepository extends JpaRepository<Apprentice, UUID> {
  List<Apprentice> findByArchivedFalseAndNameContainingIgnoreCase(String name);

  List<Apprentice> findByArchivedFalseAndSchoolIdAndNameContainingIgnoreCase(UUID schoolId, String name);
}
