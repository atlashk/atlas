package org.atlas.services.identity.application.keycloak.core.util;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapUtil;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.keycloak.representations.idm.UserRepresentation;

@UtilityClass
public class KeycloakUtil {

  public static UserEntity toUserEntity(UserRepresentation kcUser) {
    return UserEntity.builder()
        .id(kcUser.getId())
        .username(kcUser.getUsername())
        .firstName(kcUser.getFirstName())
        .lastName(kcUser.getLastName())
        .email(kcUser.getEmail())
        .phoneNumber(extractAttribute(kcUser, KeycloakUserAttribute.PHONE_NUMBER))
        .role(extractUserRole(kcUser))
        .build();
  }

  private UserRole extractUserRole(UserRepresentation kcUser) {
    List<String> realmRoles = kcUser.getRealmRoles();
    if (CollectionUtil.isEmpty(realmRoles)) {
      return null;
    }
    for (String realmRole : realmRoles) {
      if (realmRole.equalsIgnoreCase(UserRole.ADMIN.name())) {
        return UserRole.ADMIN;
      }
    }
    return UserRole.USER;
  }

  private String extractAttribute(UserRepresentation user, KeycloakUserAttribute attribute) {
    if (MapUtil.isEmpty(user.getAttributes())) {
      return null;
    }
    List<String> values = user.getAttributes().get(attribute.getName());
    if (CollectionUtil.isEmpty(values)) {
      return null;
    }
    return values.get(0);
  }
}
