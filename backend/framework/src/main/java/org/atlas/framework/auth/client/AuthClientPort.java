package org.atlas.framework.auth.client;

import org.atlas.framework.auth.client.model.CreateUserRequest;

public interface AuthClientPort {

  void createUser(CreateUserRequest request);
}
