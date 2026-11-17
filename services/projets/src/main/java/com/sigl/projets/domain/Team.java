package com.sigl.projets.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "team")
public class Team {

  @Id private UUID id;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(nullable = false)
  private String name;

  @ElementCollection
  @CollectionTable(name = "team_member", joinColumns = @JoinColumn(name = "team_id"))
  @Column(name = "user_id")
  private Set<UUID> memberIds = new HashSet<>();

  protected Team() {}

  public Team(UUID projectId, String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidRequestException("A team needs a name");
    }
    this.id = UUID.randomUUID();
    this.projectId = projectId;
    this.name = name.trim();
  }

  public void replaceMembers(Set<UUID> members) {
    memberIds.clear();
    memberIds.addAll(members);
  }

  public UUID getId() {
    return id;
  }

  public UUID getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
  }

  public Set<UUID> getMemberIds() {
    return Set.copyOf(memberIds);
  }
}
