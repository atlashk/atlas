package org.atlas.domain.user.usecase.admin.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.framework.paging.PagingRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListUserInput {

  private Integer id;

  private String keyword;

  private Role role;

  @Valid
  private PagingRequest pagingRequest;
}
