package org.atlas.framework.domain.event.contract.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.contract.order.model.OrderItem;
import org.atlas.framework.domain.event.contract.order.model.User;

@Getter
@Setter
public abstract class BaseOrderEvent extends DomainEvent {

  protected Integer orderId;
  protected User user;
  protected List<OrderItem> orderItems;
  protected BigDecimal amount;

  public void addOrderItem(OrderItem orderItem) {
    if (orderItems == null) {
      orderItems = new ArrayList<>();
    }
    orderItems.add(orderItem);
  }

  public BaseOrderEvent(String eventSource) {
    super(eventSource);
  }
}
