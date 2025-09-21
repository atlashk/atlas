package org.atlas.framework.notification.realtime.sse;

public interface SsePort {

  <T> void notify(SseNotification<T> notification);
}
