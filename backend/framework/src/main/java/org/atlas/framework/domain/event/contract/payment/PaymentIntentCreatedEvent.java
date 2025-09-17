package org.atlas.framework.domain.event.contract.payment;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.event.DomainEventType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentIntentCreatedEvent extends BasePaymentEvent {

  private String stripePaymentIntentId;
  private BigDecimal amount;
  private String currency;
  private String clientSecret;
  private String status;

  public PaymentIntentCreatedEvent(String eventSource) {
    super(eventSource);
  }

  @Override
  public DomainEventType getDomainEventType() {
    return DomainEventType.PAYMENT_INTENT_CREATED;
  }
}