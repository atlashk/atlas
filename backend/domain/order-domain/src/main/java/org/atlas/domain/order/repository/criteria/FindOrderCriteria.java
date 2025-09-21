package org.atlas.domain.order.repository.criteria;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;

@Getter
@Setter
public class FindOrderCriteria {

  private Integer orderId;
  private Integer userId;
  private Integer productId;
  private OrderStatus status;
  private Date startDate;
  private Date endDate;
}
