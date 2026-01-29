package org.atlas.services.order.application.admin.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminRetrieveOrderListInput {

  private Integer orderId;
  private Integer userId;
  private Integer productId;
  private OrderStatus status;
  private Date startDate;
  private Date endDate;
  private PagingRequest pagingRequest;
}
