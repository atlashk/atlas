package org.atlas.domain.order.repository.criteria;

import java.util.Date;
import lombok.Data;
import org.atlas.domain.order.shared.enums.OrderStatus;

@Data
public class FindOrderCriteria {

  private Integer orderId;
  private Integer userId;
  private Integer productId;
  private OrderStatus status;
  private Date startDate;
  private Date endDate;
}
