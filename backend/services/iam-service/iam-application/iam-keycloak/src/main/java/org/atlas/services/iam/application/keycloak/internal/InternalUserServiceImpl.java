package org.atlas.services.iam.application.keycloak.internal;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.services.iam.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.iam.port.in.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.iam.port.in.internal.model.InternalUserOutput;
import org.atlas.services.iam.port.in.internal.service.InternalUserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalUserServiceImpl implements InternalUserService {

  private static final String ATTR_PHONE_NUMBER = "phoneNumber";

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;

  @Override
  public List<InternalUserOutput> retrieveUserList(InternalRetrieveUserListInput input) {
    List<InternalUserOutput> outputs = new ArrayList<>();
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    for (String userId : input.getIds()) {
      UserRepresentation kcUser;
      try {
        kcUser = realm.users().get(userId).toRepresentation();
      } catch (Exception e) {
        continue;
      }
      UserRole role = extractUserRole(realm, userId);
      outputs.add(InternalUserOutput.builder()
          .userId(userId)
          .username(kcUser.getUsername())
          .firstName(kcUser.getFirstName())
          .lastName(kcUser.getLastName())
          .email(kcUser.getEmail())
          .phoneNumber(extractAttribute(kcUser, ATTR_PHONE_NUMBER))
          .role(role)
          .build());
    }
    return outputs;
  }

  private UserRole extractUserRole(RealmResource realm, String kcUserId) {
    List<RoleRepresentation> roles = realm.users().get(kcUserId)
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
}
