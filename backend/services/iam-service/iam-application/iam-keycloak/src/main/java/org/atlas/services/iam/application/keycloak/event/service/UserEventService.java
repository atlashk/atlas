package org.atlas.services.iam.application.keycloak.event.service;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.services.iam.application.keycloak.event.mapper.UserEventMapper;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.out.messaging.UserEventMessagePublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventService {

  private final UserEventMessagePublisher messagePublisher;

  public void publishUserCreatedEvent(UserEntity user) {
    UserEvent event = new UserEvent(DomainEventType.USER_CREATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }

  public void publishUserUpdatedEvent(UserEntity user) {
    UserEvent event = new UserEvent(DomainEventType.USER_UPDATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }

  public void publishUserDeletedEvent(String userId) {
    UserEvent event = new UserEvent(DomainEventType.USER_DELETED);
    event.setUserId(userId);
    messagePublisher.publish(event);
  }
}

