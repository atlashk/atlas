package org.atlas.framework.realtime.websocket;

public interface WebSocketService {

  <T> void emit(WebSocketEvent<T> event);
}
