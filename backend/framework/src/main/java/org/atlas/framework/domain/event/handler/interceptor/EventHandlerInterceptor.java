package org.atlas.framework.domain.event.handler.interceptor;

import org.atlas.framework.domain.event.DomainEvent;

public interface EventHandlerInterceptor {

  void preHandle(DomainEvent event);

  void postHandle(DomainEvent event);
}
