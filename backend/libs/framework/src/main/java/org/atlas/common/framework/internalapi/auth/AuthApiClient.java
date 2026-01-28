package org.atlas.common.framework.internalapi.auth;

import org.atlas.common.framework.internalapi.auth.model.CreateUserRequest;

public interface AuthApiClient {

  void createUser(CreateUserRequest request);
}
