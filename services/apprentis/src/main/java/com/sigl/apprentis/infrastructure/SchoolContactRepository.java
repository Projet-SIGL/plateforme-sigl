package com.sigl.apprentis.infrastructure;

import com.sigl.apprentis.domain.SchoolContact;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolContactRepository extends JpaRepository<SchoolContact, UUID> {}
