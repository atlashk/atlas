package org.atlas.framework.domain.event.contract.order.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;

  // Copy constructor
  public User(User other) {
    this.id = other.id;
    this.firstName = other.firstName;
    this.lastName = other.lastName;
    this.email = other.email;
    this.phoneNumber = other.phoneNumber;
  }
}
