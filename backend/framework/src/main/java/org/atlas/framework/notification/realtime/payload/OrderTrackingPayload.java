package org.atlas.framework.notification.realtime.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.payment.model.nextaction.NextAction;

@Getter
@Setter
@Builder
public class OrderTrackingPayload {

  private Integer orderId;
  private OrderStatus orderStatus;
  private NextAction paymentNextAction;
  private String cancellationReason;
}
