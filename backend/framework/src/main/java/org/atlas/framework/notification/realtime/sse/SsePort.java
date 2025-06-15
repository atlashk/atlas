package org.atlas.framework.notification.realtime.sse;

public interface SsePort<K> {

  void notify(SseNotification<K> notification);
}
