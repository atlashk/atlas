package org.atlas.services.user.port.in.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveUserListInput {

  private String id;

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  private UserRole role;

  private PagingRequest pagingRequest;
}
