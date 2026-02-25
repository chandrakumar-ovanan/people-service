package com.plavonra.people.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;

@Embeddable
@Data
public class PersonNameEntity implements Serializable {

  private String firstName;
  private String lastName;
}
