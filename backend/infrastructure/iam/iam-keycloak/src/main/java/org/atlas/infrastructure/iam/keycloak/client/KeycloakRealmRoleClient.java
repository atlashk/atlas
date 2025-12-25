package org.atlas.infrastructure.iam.keycloak.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.shared.Role;
import org.atlas.infrastructure.iam.keycloak.config.KeycloakProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakRealmRoleClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;

  public RoleRepresentation getRealmRole(Role role) {
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    RolesResource realmRoles = realm.roles();
    String realmRoleName = getRealmRoleName(role);
    return realmRoles
        .get(realmRoleName)
        .toRepresentation();
  }

  private String getRealmRoleName(Role role) {
    return role.name().toLowerCase();
  }
}
