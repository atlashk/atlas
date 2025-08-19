package org.atlas.domain.order.usecase.admin.model;

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
public class AdminListOrderInput {

  @Valid
  private PagingRequest pagingRequest;
}
