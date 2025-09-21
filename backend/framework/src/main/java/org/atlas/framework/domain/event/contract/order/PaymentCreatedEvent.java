package org.atlas.framework.domain.event.contract.order;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.Order;

@Getter
@Setter
public class PaymentCreatedEvent extends BaseOrderEvent {

  private Map<String, Object> paymentGatewayData;

  public PaymentCreatedEvent(String eventSource, Order order) {
    super(eventSource, order);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_CREATED;
  }
}
