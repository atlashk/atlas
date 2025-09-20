package org.atlas.domain.user.repository.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.user.shared.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindUserCriteria {

  private Integer id;
  // Username, first name, last name, email, phone number
  private String keyword;
  private Role role;
}
