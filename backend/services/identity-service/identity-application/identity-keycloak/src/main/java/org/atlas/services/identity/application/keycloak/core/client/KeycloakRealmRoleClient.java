package org.atlas.services.identity.application.keycloak.core.client;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.realm-role")
public class KeycloakRealmRoleClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;

  public RoleRepresentation getRealmRole(UserRole role) {
    RolesResource realmRoles = getRolesResource();
    return realmRoles.get(getRealmRoleName(role)).toRepresentation();
  }

  private RolesResource getRolesResource() {
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    return realm.roles();
  }

  private String getRealmRoleName(UserRole role) {
    return role.name().toLowerCase();
  }
}
