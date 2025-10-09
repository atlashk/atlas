package org.atlas.framework.realtime.sse;

public interface SseService {

  <T> void emit(SseEvent<T> event);
}
