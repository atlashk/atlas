package org.atlas.framework.auth;

import org.atlas.framework.auth.model.CreateUserRequest;

public interface AuthService {

  void createUser(CreateUserRequest request);
}
