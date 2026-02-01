package org.atlas.services.iam.port.in.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminRetrieveUserListInput {

  private Integer id;

  // Username, first name, last name, email, phone number
  private String keyword;

  private UserRole role;

  private PagingRequest pagingRequest;
}
