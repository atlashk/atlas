package org.atlas.infrastructure.messaging.internal.spring.publisher;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.messaging.InternalMessagePublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringMessagePublisherAdapter implements InternalMessagePublisherPort {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publish(DomainEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
