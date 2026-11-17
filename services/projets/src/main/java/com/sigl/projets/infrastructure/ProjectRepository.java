package com.sigl.projets.infrastructure;

import com.sigl.projets.domain.Project;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {}
