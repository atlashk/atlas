package org.atlas.libs.framework.internalapi.auth;

import org.atlas.libs.framework.internalapi.auth.model.CreateUserRequest;

public interface AuthApiClient {

  void createUser(CreateUserRequest request);
}
