package org.atlas.domain.user.usecase.admin.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.paging.PagingRequest;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListUserInput {

  private Integer id;

  // Username, first name, last name, email, phone number
  private String keyword;

  private Role role;

  @Valid
  private PagingRequest pagingRequest;
}
