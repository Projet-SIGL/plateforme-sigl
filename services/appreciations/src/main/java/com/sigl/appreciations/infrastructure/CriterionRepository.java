package com.sigl.appreciations.infrastructure;

import com.sigl.appreciations.domain.Criterion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterionRepository extends JpaRepository<Criterion, UUID> {
  List<Criterion> findByActiveTrue();
}
