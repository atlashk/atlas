package org.atlas.domain.order.usecase.front.model;

import jakarta.validation.Valid;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ListOrderInput {

  private OrderStatus status;

  private Date startDate;

  private Date endDate;

  @Valid
  private PagingRequest pagingRequest;
}
