package com.sigl.appreciations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.sigl.appreciations.domain.Appraisal;
import com.sigl.appreciations.domain.AppraisalStatus;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Runs against a real PostgreSQL (never a simulated database); skipped when Docker is absent. */
@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppraisalPersistenceIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost/issuer");
  }

  @Autowired AppraisalRepository appraisals;
  @Autowired JdbcSupervisionAdapter supervision;

  @Test
  void persistsADraftWithItsScores() {
    UUID criterion = UUID.randomUUID();
    Appraisal draft =
        Appraisal.draft(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "2030-S1");
    draft.replaceScores(Map.of(criterion, 4));

    appraisals.saveAndFlush(draft);

    Appraisal reloaded = appraisals.findById(draft.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(AppraisalStatus.DRAFT);
    assertThat(reloaded.getScores()).containsEntry(criterion, 4);
  }

  @Test
  void knowsNoSupervisionByDefault() {
    assertThat(supervision.hasSupervised(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
        .isFalse();
  }
}
