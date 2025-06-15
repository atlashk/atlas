package org.atlas.framework.notification.realtime.websocket;

public interface WebSocketPort {

  void notify(WebSocketNotification notification);
}
