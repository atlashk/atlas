package org.atlas.application.user.service;

import org.atlas.application.user.model.CreateUserInput;
import org.atlas.domain.user.entity.User;

public interface UserService {

  User retrieveUser(Integer userId);

  void createUser(CreateUserInput input);
}
