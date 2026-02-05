package org.atlas.services.iam.application.keycloak.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakRealmRoleClient;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.iam.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.iam.application.keycloak.event.service.UserEventService;
import org.atlas.services.iam.application.keycloak.core.model.CreateUserRequest;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.admin.service.AdminUserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private static final String PHONE_MAP_STORE_NAME = "iam:keycloak:phone-map";

  private static final String ATTR_PHONE_NUMBER = "phoneNumber";

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final KeycloakUserClient keycloakUserClient;
  private final KeycloakRealmRoleClient keycloakRealmRoleClient;
  private final KvStoreService kvStoreService;
  private final UserEventService userEventService;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<AdminUserOutput> retrieveUserList(AdminRetrieveUserListInput input) {
    if (input.getId() != null) {
      AdminUserOutput user = retrieveUser(input.getId());
      return PagingResult.of(List.of(user), 1L, input.getPagingRequest());
    }

    UsersResource users = realm().users();
    int first = input.getPagingRequest().getOffset();
    int max = input.getPagingRequest().getLimit();
    String keyword = StringUtil.isBlank(input.getKeyword()) ? "" : input.getKeyword();

    List<UserRepresentation> kcUsers = users.search(keyword, first, max);
    List<AdminUserOutput> outputs = kcUsers.stream()
        .map(this::toAdminUserOutputOrNull)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (input.getRole() != null) {
      outputs = outputs.stream()
          .filter(u -> input.getRole().equals(u.getRole()))
          .collect(Collectors.toList());
    }

    long totalRecords = users.count();
    return PagingResult.of(outputs, totalRecords, input.getPagingRequest());
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveUserCount() {
    return (long) realm().users().count();
  }

  @Override
  @Transactional(readOnly = true)
  public AdminUserOutput retrieveUser(String userId) {
    UserRepresentation kcUser = getKcUserOrThrow(userId);
    return AdminUserOutput.builder()
        .userId(userId)
        .username(kcUser.getUsername())
        .firstName(kcUser.getFirstName())
        .lastName(kcUser.getLastName())
        .email(kcUser.getEmail())
        .phoneNumber(extractAttribute(kcUser, ATTR_PHONE_NUMBER))
        .role(extractUserRole(userId))
        .build();
  }

  @Override
  @Transactional
  public void createUser(AdminCreateUserInput input) {
    checkValidity(input);

    HashMap<String, String> attrs = new HashMap<>();
    attrs.put(ATTR_PHONE_NUMBER, input.getPhoneNumber());

    CreateUserRequest request = CreateUserRequest.builder()
        .username(input.getUsername())
        .password(input.getPassword())
        .firstName(input.getFirstName())
        .lastName(input.getLastName())
        .email(input.getEmail())
        .role(input.getRole())
        .attributes(attrs)
        .build();

    String userId = keycloakUserClient.createUser(request);
    kvStoreService.put(PHONE_MAP_STORE_NAME, input.getPhoneNumber(), userId);

    UserEntity user = UserEntity.builder()
        .userId(userId)
        .username(input.getUsername())
        .firstName(input.getFirstName())
        .lastName(input.getLastName())
        .email(input.getEmail())
        .phoneNumber(input.getPhoneNumber())
        .role(input.getRole())
        .build();
    userEventService.publishUserCreatedEvent(user);
  }

  @Override
  @Transactional
  public void updateUser(AdminUpdateUserInput input) {
    UsersResource users = realm().users();
    UserRepresentation kcUser = getKcUserOrThrow(input.getUserId());

    String oldFirstName = kcUser.getFirstName();
    String oldLastName = kcUser.getLastName();

    kcUser.setFirstName(input.getFirstName());
    kcUser.setLastName(input.getLastName());
    users.get(input.getUserId()).update(kcUser);

    if (StringUtil.isNotBlank(input.getPassword())) {
      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setType(CredentialRepresentation.PASSWORD);
      credential.setValue(input.getPassword());
      credential.setTemporary(Boolean.FALSE);
      users.get(input.getUserId()).resetPassword(credential);
    }

    RoleRepresentation desiredRole = keycloakRealmRoleClient.getRealmRole(input.getRole());
    RoleScopeResource roleScope = users.get(input.getUserId()).roles().realmLevel();
    List<RoleRepresentation> current = roleScope.listEffective();
    List<RoleRepresentation> toRemove = current.stream()
        .filter(role -> role != null && role.getName() != null
            && (role.getName().equalsIgnoreCase(UserRole.ADMIN.name())
            || role.getName().equalsIgnoreCase(UserRole.USER.name())))
        .collect(Collectors.toList());
    if (!toRemove.isEmpty()) {
      roleScope.remove(toRemove);
    }
    roleScope.add(List.of(desiredRole));

    if (!Objects.equals(oldFirstName, input.getFirstName())
        || !Objects.equals(oldLastName, input.getLastName())) {
      UserEntity user = UserEntity.builder()
          .userId(input.getUserId())
          .username(kcUser.getUsername())
          .firstName(input.getFirstName())
          .lastName(input.getLastName())
          .email(kcUser.getEmail())
          .phoneNumber(extractAttribute(kcUser, ATTR_PHONE_NUMBER))
          .role(input.getRole())
          .build();
      userEventService.publishUserUpdatedEvent(user);
    }
  }

  @Override
  @Transactional
  public void deleteUser(String userId) {
    UserRepresentation kcUser = getKcUserOrThrow(userId);
    String phoneNumber = extractAttribute(kcUser, ATTR_PHONE_NUMBER);

    realm().users().delete(userId);
    if (StringUtil.isNotBlank(phoneNumber)) {
      kvStoreService.delete(PHONE_MAP_STORE_NAME, phoneNumber);
    }
    userEventService.publishUserDeletedEvent(userId);
  }

  private RealmResource realm() {
    return keycloak.realm(keycloakProps.getRealm());
  }

  private UserRepresentation getKcUserOrThrow(String kcUserId) {
    try {
      return realm().users().get(kcUserId).toRepresentation();
    } catch (Exception e) {
      throw new DomainException(DomainError.USER_NOT_FOUND);
    }
  }

  private UserRole extractUserRole(String kcUserId) {
    List<RoleRepresentation> roles = realm().users().get(kcUserId)
        .roles()
        .realmLevel()
        .listEffective();
    for (RoleRepresentation role : roles) {
      if (role != null && role.getName() != null
          && role.getName().equalsIgnoreCase(UserRole.ADMIN.name())) {
        return UserRole.ADMIN;
      }
    }
    return UserRole.USER;
  }

  private AdminUserOutput toAdminUserOutputOrNull(UserRepresentation kcUser) {
    String userId = kcUser.getId();
    if (StringUtil.isBlank(userId)) {
      return null;
    }
    return AdminUserOutput.builder()
        .userId(userId)
        .username(kcUser.getUsername())
        .firstName(kcUser.getFirstName())
        .lastName(kcUser.getLastName())
        .email(kcUser.getEmail())
        .phoneNumber(extractAttribute(kcUser, ATTR_PHONE_NUMBER))
        .role(extractUserRole(userId))
        .build();
  }

  private String extractAttribute(UserRepresentation user, String attribute) {
    if (user.getAttributes() == null) {
      return null;
    }
    List<String> values = user.getAttributes().get(attribute);
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }

  private void checkValidity(AdminCreateUserInput input) {
    UsersResource users = realm().users();

    List<UserRepresentation> usernameMatches = users.search(input.getUsername(), 0, 20);
    if (usernameMatches.stream().anyMatch(
        user -> user != null && StringUtil.isNotBlank(user.getUsername())
            && user.getUsername().equalsIgnoreCase(input.getUsername()))) {
      throw new DomainException(DomainError.USERNAME_ALREADY_EXISTS);
    }

    List<UserRepresentation> emailMatches = users.search(input.getEmail(), 0, 20);
    if (emailMatches.stream().anyMatch(
        user -> user != null && StringUtil.isNotBlank(user.getEmail())
            && user.getEmail().equalsIgnoreCase(input.getEmail()))) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }

    if (kvStoreService.exists(PHONE_MAP_STORE_NAME, input.getPhoneNumber())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
