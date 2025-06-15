package org.atlas.domain.order.usecase.front.model;

import jakarta.validation.Valid;
import java.util.Date;
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

  private Date startDate;

  private Date endDate;

  @Valid
  private PagingRequest pagingRequest;
}
