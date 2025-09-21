package org.atlas.infrastructure.notification.websocket.config;

import static org.atlas.infrastructure.notification.websocket.config.WebSocketServerConfig.DESTINATION_PREFIX;

import lombok.experimental.UtilityClass;
import org.atlas.framework.notification.realtime.payload.OrderTrackingPayload;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;

@UtilityClass
public class WebSocketDestinationResolver {

  public static String resolve(WebSocketNotification<?> notification) {
    return switch (notification.getType()) {
      case ORDER_TRACKING -> {
        OrderTrackingPayload orderTrackingPayload = (OrderTrackingPayload) notification.getPayload();
        yield String.format("%s/orders/%d/tracking", DESTINATION_PREFIX,
            orderTrackingPayload.getOrderId());
      }
      // Add more cases here for different notification types if needed
    };
  }
}
