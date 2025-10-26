package org.atlas.domain.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserVO {

  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
}
