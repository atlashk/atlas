package org.atlas.infrastructure.notification.websocket.config;

import static org.atlas.infrastructure.notification.websocket.config.WebSocketServerConfig.DESTINATION_PREFIX;

import lombok.experimental.UtilityClass;
import org.atlas.framework.notification.common.NotificationType;
import org.atlas.framework.notification.realtime.payload.OrderCanceledPayload;
import org.atlas.framework.notification.realtime.websocket.WebSocketNotification;

@UtilityClass
public class WebSocketDestinationResolver {

  public static String resolve(WebSocketNotification notification) {
    if (notification == null) {
      throw new IllegalArgumentException("Notification cannot be null.");
    }

    NotificationType notificationType = notification.getType();
    if (notificationType == null) {
      throw new IllegalArgumentException("Notification type cannot be null.");
    }

    switch (notificationType) {
      case ORDER_STATUS_CHANGED:
        if (!(notification.getPayload() instanceof OrderCanceledPayload payload)) {
          throw new IllegalArgumentException(
              "Payload must be of type OrderStatusChangedPayload for ORDER_STATUS_CHANGED notification.");
        }
        return String.format("%s/orders/%d/status", DESTINATION_PREFIX, payload.getOrderId());
      // Add more cases here for different notification types if needed
      default:
        throw new IllegalArgumentException("Unknown notification type: " + notificationType);
    }
  }
}
