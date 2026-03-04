package org.atlas.services.identity.application.keycloak.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.identity.application.keycloak.user.mapper.UserAdminMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.domain.error.DomainError;
import org.atlas.services.identity.domain.exception.DomainException;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.atlas.services.identity.port.in.user.service.UserAdminService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {

  private final KeycloakUserClient keycloakUserClient;

  @Override
  public PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    RetrieveUserListRequest kcRequest = UserAdminMapper.INSTANCE.toRetrieveUserListRequest(input);
    int first = input.getPagingRequest().getOffset();
    int max = input.getPagingRequest().getLimit();
    PagingRequest pagingRequest = PagingRequest.of(first,
        max + 1); // Fetch one extra to determine if there's a next page
    kcRequest.setPagingRequest(pagingRequest);
    log.info("Retrieving user list with criteria: userId={}, username={}, firstName={}, lastName={}, email={}, offset={}, limit={}",
        kcRequest.getUserId(), kcRequest.getUsername(), kcRequest.getFirstName(),
        kcRequest.getLastName(), kcRequest.getEmail(), first, max);
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
  public UserOutput retrieveUser(String userId) {
    return keycloakUserClient.retrieveUser(userId)
        .map(UserAdminMapper.INSTANCE::toUserOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  public String createUser(CreateUserInput input) {
    checkValidity(input);

    UserEntity user = UserAdminMapper.INSTANCE.toUser(input);
    return keycloakUserClient.createUser(user, input.getPassword());
  }

  @Override
  public void updateUser(UpdateUserInput input) {
    UserEntity user = keycloakUserClient.retrieveUser(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    UserAdminMapper.INSTANCE.merge(input, user);
    keycloakUserClient.updateUser(user);
  }

  @Override
  public void deleteUser(String id) {
    keycloakUserClient.deleteUser(id);
  }

  @Override
  public boolean existsUser(String username) {
    return keycloakUserClient.existsByUsername(username);
  }

  @Override
  public Long retrieveTotalUserCount() {
    return keycloakUserClient.retrieveTotalUserCount();
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
}
