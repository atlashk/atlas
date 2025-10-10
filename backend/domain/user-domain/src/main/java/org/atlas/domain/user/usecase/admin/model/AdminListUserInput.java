package org.atlas.domain.user.usecase.admin.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminListUserInput {

  private Integer id;

  // Username, first name, last name, email, phone number
  private String keyword;

  private Role role;

  @Valid
  private PagingRequest pagingRequest;
}
