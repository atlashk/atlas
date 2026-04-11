package org.atlas.services.user.infrastructure.idp.adapter;

import lombok.RequiredArgsConstructor;
import org.atlas.services.user.port.out.idp.IdpService;
import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.infrastructure.idp.client.KeycloakUserClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakIdpAdapter implements IdpService {

  private final KeycloakUserClient keycloakUserClient;
  
  @Override
  public void createUser(User user, String password) {
    keycloakUserClient.createUser(user, password);
  }

  @Override
  public void updateUser(User user) {
    keycloakUserClient.updateUser(user);
  }

  @Override
  public void deleteUser(String idpUserId) {
    keycloakUserClient.deleteUser(idpUserId);
  }

  @Override
  public boolean existsByEmail(String email) {
    return keycloakUserClient.existsByEmail(email);
  }
}
