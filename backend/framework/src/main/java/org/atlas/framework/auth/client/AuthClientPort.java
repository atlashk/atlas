package org.atlas.framework.auth.client;

import org.atlas.framework.auth.client.model.CreateAuthUserRequest;

public interface AuthClientPort {

  void createAuthUser(CreateAuthUserRequest request);
}
