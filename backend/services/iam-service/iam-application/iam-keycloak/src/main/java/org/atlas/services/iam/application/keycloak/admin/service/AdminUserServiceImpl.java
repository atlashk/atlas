package org.atlas.services.iam.application.keycloak.admin.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.iam.application.keycloak.admin.mapper.AdminUserMapper;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.iam.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.iam.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.iam.application.keycloak.event.service.UserEventService;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.admin.service.AdminUserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final KeycloakUserClient keycloakUserClient;
  private final UserEventService userEventService;

  @Override
  public PagingResult<AdminUserOutput> retrieveUserList(AdminRetrieveUserListInput input) {
    RetrieveUserListRequest keycloakRequest =
        AdminUserMapper.INSTANCE.toKeycloakRetrieveUserListRequest(input);
    int first = input.getPagingRequest().getOffset();
    int max = input.getPagingRequest().getLimit();
    PagingRequest pagingRequest = PagingRequest.of(first,
        max + 1); // Fetch one extra to determine if there's a next page
    keycloakRequest.setPagingRequest(pagingRequest);
    List<UserEntity> userList = keycloakUserClient.retrieveUserList(keycloakRequest);

    // Process paging
    boolean hasNext = userList.size() > max;
    if (hasNext) {
      userList = userList.subList(0, max);
    }
    return PagingResult.of(userList, input.getPagingRequest(), hasNext)
        .map(AdminUserMapper.INSTANCE::toAdminUserOutput);
  }

  @Override
  public Long retrieveUserCount() {
    return keycloakUserClient.retrieveUserCount();
  }

  @Override
  public AdminUserOutput retrieveUser(String userId) {
    return keycloakUserClient.retrieveUser(userId)
        .map(AdminUserMapper.INSTANCE::toAdminUserOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  public void createUser(AdminCreateUserInput input) {
    checkValidity(input);

    UserEntity user = AdminUserMapper.INSTANCE.toUser(input);
    keycloakUserClient.createUser(user, input.getPassword());

    userEventService.publishUserCreatedEvent(user);
  }

  @Override
  public void updateUser(AdminUpdateUserInput input) {
    UserEntity user = keycloakUserClient.retrieveUser(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    boolean shouldPublishEvent = !Objects.equals(input.getFirstName(), user.getFirstName()) ||
        !Objects.equals(input.getLastName(), user.getLastName());

    AdminUserMapper.INSTANCE.merge(input, user);
    keycloakUserClient.updateUser(user, input.getPassword());

    if (shouldPublishEvent) {
      userEventService.publishUserUpdatedEvent(user);
    }
  }

  @Override
  public void deleteUser(String userId) {
    keycloakUserClient.deleteUser(userId);

    userEventService.publishUserDeletedEvent(userId);
  }

  private void checkValidity(AdminCreateUserInput input) {
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
}
