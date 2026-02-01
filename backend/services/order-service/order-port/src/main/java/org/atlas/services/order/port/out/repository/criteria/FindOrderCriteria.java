package org.atlas.services.order.port.out.repository.criteria;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.order.OrderStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
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
