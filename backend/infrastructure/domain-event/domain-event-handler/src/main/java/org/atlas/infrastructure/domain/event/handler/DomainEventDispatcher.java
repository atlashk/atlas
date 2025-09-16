package org.atlas.infrastructure.domain.event.handler;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.util.ReflectionUtil;
import org.atlas.infrastructure.application.context.ApplicationContextService;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventDispatcher {

  private final ApplicationContextService applicationContextService;

  public void dispatch(Object messagePayload) {
    // Find the handler based on domain event type
    DomainEvent domainEvent = (DomainEvent) messagePayload;
    DomainEventType domainEventType = domainEvent.getDomainEventType();
    Optional<Object> domainEventHandlerOpt = applicationContextService.getBeanByAnnotationAttribute(
        DomainEventHandler.class, DomainEventType.class, "type", domainEventType);
    if (domainEventHandlerOpt.isEmpty()) {
      // Skip handling
      return;
    }
    Object domainEventHandler = domainEventHandlerOpt.get();

    // Get the target class to handle CGLIB proxies
    Class<?> targetClass = AopUtils.isAopProxy(domainEventHandler)
        ? AopUtils.getTargetClass(domainEventHandler)
        : domainEventHandler.getClass();

    ReflectionUtil.invokeMethod(domainEventHandler, targetClass, "handle",
        Map.of(domainEvent.getClass(), domainEvent));
  }
}
