package org.atlas.domain.order.repository;

import java.util.Date;
import lombok.Data;
import org.atlas.domain.order.shared.enums.OrderStatus;

@Data
public class FindOrderCriteria {

  private Integer id;
  private Integer userId;
  private OrderStatus status;
  private Date startDate;
  private Date endDate;
}
