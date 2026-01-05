package org.atlas.common.framework.domain.common.event.handler.interceptor;

import org.atlas.common.framework.domain.common.event.DomainEvent;

public interface EventHandlerInterceptor {

  void preHandle(DomainEvent event);

  void postHandle(DomainEvent event);
}
