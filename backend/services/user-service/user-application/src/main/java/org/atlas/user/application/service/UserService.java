package org.atlas.user.application.service;

import org.atlas.user.application.model.CreateUserInput;
import org.atlas.user.domain.entity.User;

public interface UserService {

  User retrieveUser(Integer userId);

  void createUser(CreateUserInput input);
}
