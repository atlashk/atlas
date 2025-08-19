package org.atlas.domain.order.usecase.front.model;

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
public class FrontListOrderInput {

  @Valid
  private PagingRequest pagingRequest;
}
