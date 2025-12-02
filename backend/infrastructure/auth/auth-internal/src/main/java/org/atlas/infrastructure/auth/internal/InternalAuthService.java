package org.atlas.infrastructure.auth.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.auth.AuthService;
import org.atlas.framework.auth.model.CreateUserRequest;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalAuthService implements AuthService {

  private final AuthApiClient authApiClient;

  @Override
  public void createUser(CreateUserRequest request) {
    org.atlas.framework.internalapi.auth.model.CreateUserRequest internalRequest =
        InternalAuthMapper.INSTANCE.toInternalCreateUserRequest(request);
    authApiClient.createUser(internalRequest);
  }
}
