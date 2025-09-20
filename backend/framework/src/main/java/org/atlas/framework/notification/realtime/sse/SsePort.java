package org.atlas.framework.notification.realtime.sse;

public interface SsePort {

  void notify(SseNotification notification);
}
