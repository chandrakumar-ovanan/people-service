package com.plavonra.people.entity;

import static jakarta.persistence.EnumType.STRING;

import com.plavonra.common.audit.Action;
import com.plavonra.common.audit.Auditable;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "person", schema = "person")
@Data
@EqualsAndHashCode(callSuper = true)
public class PersonEntity extends Auditable implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String email;
  @Embedded private PersonNameEntity name;
  private String age;
  private String favoriteColor;

  @Enumerated(STRING)
  private Action action;

  private String note;

  @OneToOne(mappedBy = "person", fetch = FetchType.LAZY)
  @EqualsAndHashCode.Exclude
  private PersonNoteAnalysisEntity noteAnalysis;
}
