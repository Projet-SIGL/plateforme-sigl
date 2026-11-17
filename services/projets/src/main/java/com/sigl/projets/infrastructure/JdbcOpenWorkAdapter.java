package com.sigl.projets.infrastructure;

import com.sigl.projets.application.OpenWorkPort;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class JdbcOpenWorkAdapter implements OpenWorkPort {

  private final JdbcTemplate jdbc;

  JdbcOpenWorkAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public boolean hasOpenWork(UUID projectId) {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM task WHERE project_id = ? AND status <> 'DONE'",
            Integer.class,
            projectId);
    return count != null && count > 0;
  }
}
