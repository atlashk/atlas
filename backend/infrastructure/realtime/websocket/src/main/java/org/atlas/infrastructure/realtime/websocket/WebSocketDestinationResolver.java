package org.atlas.infrastructure.realtime.websocket;

import lombok.experimental.UtilityClass;
import org.atlas.framework.realtime.websocket.WebSocketEvent;

@UtilityClass
public class WebSocketDestinationResolver {

  public static <T> String resolve(WebSocketEvent<T> event) {
    return "";
  }
}
