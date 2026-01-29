package org.atlas.libs.framework.domain.common.event.handler.interceptor;

import org.atlas.libs.framework.domain.common.event.DomainEvent;

public interface EventHandlerInterceptor {

  void preHandle(DomainEvent event);

  void postHandle(DomainEvent event);
}
