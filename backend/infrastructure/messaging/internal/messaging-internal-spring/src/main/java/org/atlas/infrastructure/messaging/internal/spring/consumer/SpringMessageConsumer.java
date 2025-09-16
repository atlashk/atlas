package org.atlas.infrastructure.messaging.internal.spring.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.infrastructure.domain.event.handler.DomainEventDispatcher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringMessageConsumer {

  private final DomainEventDispatcher domainEventDispatcher;

  @Async("springMessageConsumerTaskExecutor")
  @EventListener
  public void consumeMessage(DomainEvent event) {
    log.info("Consumed message: {}", event);

    // Handle message
    domainEventDispatcher.dispatch(event);
  }
}
