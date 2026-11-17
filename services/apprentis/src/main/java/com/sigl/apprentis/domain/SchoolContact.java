package com.sigl.apprentis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** A person of a school that follows apprentices. */
@Entity
@Table(name = "school_contact")
public class SchoolContact {

  @Id private UUID id;

  @Column(name = "school_id", nullable = false)
  private UUID schoolId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  private String phone;

  protected SchoolContact() {}

  public SchoolContact(UUID schoolId, String name, String email, String phone) {
    this.id = UUID.randomUUID();
    this.schoolId = schoolId;
    update(name, email, phone);
  }

  public void update(String name, String email, String phone) {
    if (name == null || name.isBlank()) {
      throw new InvalidRequestException("The contact name is required");
    }
    if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
      throw new InvalidRequestException("The contact email is invalid");
    }
    this.name = name.trim();
    this.email = email.trim();
    this.phone = phone;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSchoolId() {
    return schoolId;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }
}
