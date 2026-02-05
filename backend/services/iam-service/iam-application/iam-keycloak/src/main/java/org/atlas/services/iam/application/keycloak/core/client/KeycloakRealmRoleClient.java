package org.atlas.services.iam.application.keycloak.core.client;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.services.iam.application.keycloak.core.config.KeycloakProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakRealmRoleClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;

  public RoleRepresentation getRealmRole(UserRole role) {
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    RolesResource realmRoles = realm.roles();
    return realmRoles.get(getRealmRoleName(role)).toRepresentation();
  }

  private String getRealmRoleName(UserRole role) {
    return role.name().toLowerCase();
  }
}
