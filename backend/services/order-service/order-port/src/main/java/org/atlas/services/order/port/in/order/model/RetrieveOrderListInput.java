package org.atlas.services.order.port.in.order.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveOrderListInput {

  private OrderStatus status;
  private Date startDate;
  private Date endDate;
  private PagingRequest pagingRequest;
}
