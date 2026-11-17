package com.sigl.appreciations.infrastructure;

import com.sigl.appreciations.domain.AppraisalSummary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<AppraisalSummary, UUID> {}
