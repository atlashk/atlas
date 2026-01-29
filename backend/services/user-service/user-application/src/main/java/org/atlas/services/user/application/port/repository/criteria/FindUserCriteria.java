package org.atlas.services.user.application.port.repository.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.Role;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindUserCriteria {

  private Integer id;
  // Username, first name, last name, email, phone number
  private String keyword;
  private Role role;
}
