package org.atlas.services.identity.application.keycloak.user.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.identity.application.keycloak.user.mapper.UserAdminMapper;
import org.atlas.services.identity.application.keycloak.user.mapper.UserEventMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.atlas.services.identity.port.in.user.service.UserAdminService;
import org.atlas.services.identity.port.out.messaging.UserEventMessagePublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

  private final KeycloakUserClient keycloakUserClient;
  private final UserEventMessagePublisher messagePublisher;

  @Override
  public PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    RetrieveUserListRequest kcRequest = UserAdminMapper.INSTANCE.toRetrieveUserListRequest(input);
    int first = input.getPagingRequest().getOffset();
    int max = input.getPagingRequest().getLimit();
    PagingRequest pagingRequest = PagingRequest.of(first,
        max + 1); // Fetch one extra to determine if there's a next page
    kcRequest.setPagingRequest(pagingRequest);
    List<UserEntity> userList = keycloakUserClient.retrieveUserList(kcRequest);

    // Process paging
    boolean hasNext = userList.size() > max;
    if (hasNext) {
      userList = userList.subList(0, max);
    }
    return PagingResult.of(userList, input.getPagingRequest(), hasNext)
        .map(UserAdminMapper.INSTANCE::toUserOutput);
  }

  @Override
  public Long retrieveUserCount() {
    return keycloakUserClient.retrieveUserCount();
  }

  @Override
  public UserOutput retrieveUser(String userId) {
    return keycloakUserClient.retrieveUser(userId)
        .map(UserAdminMapper.INSTANCE::toUserOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  public void createUser(CreateUserInput input) {
    checkValidity(input);

    UserEntity user = UserAdminMapper.INSTANCE.toUser(input);
    keycloakUserClient.createUser(user, input.getPassword());

    publishUserCreatedEvent(user);
  }

  @Override
  public void updateUser(UpdateUserInput input) {
    UserEntity user = keycloakUserClient.retrieveUser(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    boolean shouldPublishEvent = !Objects.equals(input.getFirstName(), user.getFirstName()) ||
        !Objects.equals(input.getLastName(), user.getLastName());

    UserAdminMapper.INSTANCE.merge(input, user);
    keycloakUserClient.updateUser(user);

    if (shouldPublishEvent) {
      publishUserUpdatedEvent(user);
    }
  }

  @Override
  public void deleteUser(String id) {
    keycloakUserClient.deleteUser(id);

    publishUserDeletedEvent(id);
  }

  @Override
  public boolean existsUser(String username) {
    return keycloakUserClient.existsByUsername(username);
  }

  private void checkValidity(CreateUserInput input) {
    if (keycloakUserClient.existsByUsername(input.getUsername())) {
      throw new DomainException(DomainError.USERNAME_ALREADY_EXISTS);
    }
    if (keycloakUserClient.existsByEmail(input.getEmail())) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }
    if (keycloakUserClient.existsByAttribute(KeycloakUserAttribute.PHONE_NUMBER,
        input.getPhoneNumber())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }

  private void publishUserCreatedEvent(UserEntity user) {
    UserEvent event = new UserEvent(DomainEventType.USER_CREATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }

  private void publishUserUpdatedEvent(UserEntity user) {
    UserEvent event = new UserEvent(DomainEventType.USER_UPDATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }

  private void publishUserDeletedEvent(String userId) {
    UserEvent event = new UserEvent(DomainEventType.USER_DELETED);
    event.setUserId(userId);
    messagePublisher.publish(event);
  }
}
