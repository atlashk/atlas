package org.atlas.infrastructure.notification.websocket.config;

import static org.atlas.infrastructure.notification.websocket.config.WebSocketServerConfig.DESTINATION_PREFIX;

import lombok.experimental.UtilityClass;
import org.atlas.framework.domain.event.contract.order.OrderCanceledEvent;
import org.atlas.framework.domain.event.contract.order.OrderFulfilledEvent;
import org.atlas.framework.domain.event.contract.payment.PaymentCreatedEvent;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;

@UtilityClass
public class WebSocketDestinationResolver {

  public static String resolve(WebSocketNotification notification) {
    return switch (notification.getType()) {
      case PAYMENT_CREATED -> {
        PaymentCreatedEvent paymentCreatedEvent = (PaymentCreatedEvent) notification.getPayload();
        yield String.format("%s/orders/%d",
            DESTINATION_PREFIX, paymentCreatedEvent.getOrderId());
      }
      case ORDER_FULFILLED -> {
        OrderFulfilledEvent orderFulfilledEvent = (OrderFulfilledEvent) notification.getPayload();
        yield String.format("%s/orders/%d", DESTINATION_PREFIX,
            orderFulfilledEvent.getOrder().getOrderId());
      }
      case ORDER_CANCELED -> {
        OrderCanceledEvent orderCanceledEvent = (OrderCanceledEvent) notification.getPayload();
        yield String.format("%s/orders/%d", DESTINATION_PREFIX,
            orderCanceledEvent.getOrder().getOrderId());
      }
      // Add more cases here for different notification types if needed
    };
  }
}
