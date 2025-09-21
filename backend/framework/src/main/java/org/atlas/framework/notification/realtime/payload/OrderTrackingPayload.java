package org.atlas.framework.notification.realtime.payload;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;

@Getter
@Setter
@Builder
public class OrderTrackingPayload {

  private Integer orderId;
  private OrderStatus orderStatus;
  private Map<String, Object> paymentGatewayData;
  private String cancellationReason;
}
