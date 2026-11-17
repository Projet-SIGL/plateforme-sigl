package com.sigl.appreciations.infrastructure;

import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppraisalRepository extends JpaRepository<Appraisal, UUID> {
  List<Appraisal> findByApprenticeId(UUID apprenticeId);

  List<Appraisal> findByApprenticeIdAndPeriodAndStatus(
      UUID apprenticeId, String period, AppraisalStatus status);
}
