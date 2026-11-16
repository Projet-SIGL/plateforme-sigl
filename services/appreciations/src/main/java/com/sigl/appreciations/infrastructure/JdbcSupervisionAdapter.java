package com.sigl.appreciations.infrastructure;

import com.sigl.appreciations.application.SupervisionPort;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class JdbcSupervisionAdapter implements SupervisionPort {

  private final JdbcTemplate jdbc;

  JdbcSupervisionAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public boolean hasSupervised(UUID evaluatorId, UUID apprenticeId, UUID projectId) {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM effective_supervision"
                + " WHERE evaluator_id = ? AND apprentice_id = ? AND project_id = ?",
            Integer.class,
            evaluatorId,
            apprenticeId,
            projectId);
    return count != null && count > 0;
  }
}
