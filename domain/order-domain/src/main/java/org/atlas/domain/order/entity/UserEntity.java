package org.atlas.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
}
