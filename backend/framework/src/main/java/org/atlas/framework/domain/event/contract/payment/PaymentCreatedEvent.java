package org.atlas.framework.domain.event.contract.payment;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.order.model.Order;

@Getter
@Setter
public class PaymentCreatedEvent extends BasePaymentEvent {

  private Order order;
  private Map<String, Object> paymentData;

  public PaymentCreatedEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_INTENT_CREATED;
  }
}