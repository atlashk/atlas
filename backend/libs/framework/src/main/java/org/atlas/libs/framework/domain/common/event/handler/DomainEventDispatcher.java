package org.atlas.libs.framework.domain.common.event.handler;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.event.DomainEvent;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.util.ReflectionUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventDispatcher {

  private final ApplicationContext applicationContext;

  public void dispatch(Object messagePayload) {
    // Find the handler based on domain event type
    DomainEvent domainEvent = (DomainEvent) messagePayload;
    DomainEventType eventType = domainEvent.getEventType();

    // Get all beans annotated with @DomainEventHandler
    Map<String, Object> handlers = applicationContext.getBeansWithAnnotation(
        DomainEventHandler.class);

    Optional<Object> domainEventHandlerOpt = handlers.values().stream()
        .filter(handler -> {
          DomainEventHandler annotation = handler.getClass()
              .getAnnotation(DomainEventHandler.class);
          return annotation != null && eventType.equals(annotation.type());
        })
        .findFirst();

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
