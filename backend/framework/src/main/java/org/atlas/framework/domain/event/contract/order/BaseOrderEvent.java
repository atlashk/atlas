package org.atlas.framework.domain.event.contract.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.User;

@NoArgsConstructor
@Getter
@Setter
public abstract class BaseOrderEvent extends DomainEvent {

  protected Integer orderId;
  protected User user;
  protected List<OrderItem> orderItems;
  protected BigDecimal amount;
  protected String canceledReason;
  protected Date createdAt;
  protected String error;

  public BaseOrderEvent(String eventSource) {
    super(eventSource);
  }
}
