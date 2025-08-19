package org.atlas.domain.user.usecase.admin.model;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.framework.paging.PagingRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListUserInput {

  @Valid
  private PagingRequest pagingRequest;
}
