package com.sigl.apprentis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** An apprentice file; archived at the end of the contract, never deleted (rules R09, R10). */
@Entity
@Table(name = "apprentice")
public class Apprentice {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "school_id", nullable = false)
  private UUID schoolId;

  @Column(name = "master_id")
  private UUID masterId;

  @Column(nullable = false)
  private boolean archived;

  protected Apprentice() {}

  public Apprentice(String name, UUID schoolId, UUID masterId) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.schoolId = schoolId;
    this.masterId = masterId;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public UUID getSchoolId() {
    return schoolId;
  }

  public UUID getMasterId() {
    return masterId;
  }

  public boolean isArchived() {
    return archived;
  }
}
