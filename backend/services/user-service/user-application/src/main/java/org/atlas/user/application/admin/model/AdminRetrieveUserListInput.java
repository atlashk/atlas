package org.atlas.user.application.admin.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.domain.user.Role;
import org.atlas.common.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminRetrieveUserListInput {

  private Integer id;

  // Username, first name, last name, email, phone number
  private String keyword;

  private Role role;

  @Valid
  private PagingRequest pagingRequest;
}
