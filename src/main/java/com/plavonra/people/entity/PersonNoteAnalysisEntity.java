package com.plavonra.people.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(
    name = "person_note_analysis",
    schema = "person",
    uniqueConstraints = @UniqueConstraint(columnNames = "person_id"))
@Data
public class PersonNoteAnalysisEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  private PersonEntity person;

  @Column(length = 50)
  private String topic;

  @Column(length = 50)
  private String sentiment;

  @Column(length = 100)
  private String modelName;

  private Instant analyzedAt;
}
