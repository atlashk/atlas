package org.atlas.services.user.port.out.idp;

import org.atlas.services.user.domain.entity.User;

public interface IdpService {

  void createUser(User user, String password);

  void updateUser(User user);

  void deleteUser(String idpUserId);

  boolean existsByEmail(String email);
}
