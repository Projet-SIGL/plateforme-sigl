package com.sigl.projets.infrastructure;

import com.sigl.projets.domain.Team;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {
  boolean existsByProjectIdAndName(UUID projectId, String name);

  Optional<Team> findByIdAndProjectId(UUID id, UUID projectId);
}
