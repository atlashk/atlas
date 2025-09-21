package org.atlas.framework.notification.realtime.websocket;

public interface WebSocketPort {

  <T> void notify(WebSocketNotification<T> notification);
}
