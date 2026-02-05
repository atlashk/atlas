package org.atlas.services.iam.application.keycloak.front;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakAuthenticationClient;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.iam.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.iam.application.keycloak.event.service.UserEventService;
import org.atlas.services.iam.application.keycloak.core.model.CreateUserRequest;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.front.model.ChangePasswordInput;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;
import org.atlas.services.iam.port.in.front.service.UserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final String PHONE_MAP_STORE_NAME = "iam:keycloak:phone-map";

  private static final String ATTR_PHONE_NUMBER = "phoneNumber";

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final KeycloakUserClient keycloakUserClient;
  private final KeycloakAuthenticationClient keycloakAuthenticationClient;
  private final KvStoreService kvStoreService;
  private final UserEventService userEventService;

  @Override
  @Transactional
  public void register(RegisterInput input) {
    checkValidity(input);

    if (kvStoreService.exists(PHONE_MAP_STORE_NAME, input.getPhoneNumber())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }

    Map<String, String> attrs = new HashMap<>();
    attrs.put(ATTR_PHONE_NUMBER, input.getPhoneNumber());

    CreateUserRequest request = CreateUserRequest.builder()
        .username(input.getUsername())
        .password(input.getPassword())
        .firstName(input.getFirstName())
        .lastName(input.getLastName())
        .email(input.getEmail())
        .role(UserRole.USER)
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
        .role(UserRole.USER)
        .build();
    userEventService.publishUserCreatedEvent(user);
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileOutput retrieveProfile() {
    String userId = Contexts.getUserId();
    UserRepresentation kcUser = getKcUserOrThrow(userId);
    UserRole role = extractUserRole(userId);

    return ProfileOutput.builder()
        .userId(userId)
        .username(kcUser.getUsername())
        .firstName(kcUser.getFirstName())
        .lastName(kcUser.getLastName())
        .email(kcUser.getEmail())
        .phoneNumber(extractAttribute(kcUser, ATTR_PHONE_NUMBER))
        .role(role)
        .build();
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordInput input) {
    String userId = Contexts.getUserId();
    UserRepresentation kcUser = getKcUserOrThrow(userId);

    if (StringUtil.isBlank(kcUser.getUsername())) {
      throw new DomainException(DomainError.USER_NOT_FOUND);
    }

    keycloakAuthenticationClient.login(kcUser.getUsername(), input.getOldPassword());

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(input.getNewPassword());
    credential.setTemporary(Boolean.FALSE);
    realm().users().get(userId).resetPassword(credential);
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
    UsersResource users = realm().users();
    List<RoleRepresentation> roles = users.get(kcUserId)
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

  private void checkValidity(RegisterInput input) {
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
  }
}
