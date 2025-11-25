package org.atlas.framework.internalapi.auth;

import org.atlas.framework.internalapi.auth.model.CreateUserRequest;

public interface AuthApiClient {

  void createUser(CreateUserRequest request);
}
