package com.sigl.apprentis.infrastructure;

import com.sigl.apprentis.domain.CalendarPeriod;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarPeriodRepository extends JpaRepository<CalendarPeriod, UUID> {
  List<CalendarPeriod> findByApprenticeIdOrderByStartDate(UUID apprenticeId);
}
