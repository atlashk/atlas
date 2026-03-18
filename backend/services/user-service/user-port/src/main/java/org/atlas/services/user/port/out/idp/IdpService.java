package org.atlas.services.user.port.out.idp;

import org.atlas.services.user.domain.entity.UserEntity;

public interface IdpService {

  void createUser(UserEntity user, String password);

  void updateUser(UserEntity user);

  void deleteUser(String idpUserId);
}
