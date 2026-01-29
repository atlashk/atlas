package org.atlas.services.user.application.service;

import org.atlas.services.user.application.model.CreateUserInput;
import org.atlas.services.user.domain.entity.User;

public interface UserService {

  User retrieveUser(Integer userId);

  void createUser(CreateUserInput input);
}
