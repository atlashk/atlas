package org.atlas.edge.auth.springsecurityjwt.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.OneTimeTokenLoginUseCase;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.domain.auth.usecase.authentication.model.OneTimeTokenLoginInput;
import org.atlas.edge.auth.springsecurityjwt.service.LoginService;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;

@UseCaseHandler
@RequiredArgsConstructor
public class OneTimeTokenLoginUseCaseHandler implements OneTimeTokenLoginUseCase {

  private final LoginService loginService;

  @Override
  public LoginOutput handle(OneTimeTokenLoginInput input) throws Exception {
    OneTimeTokenAuthenticationToken authenticationRequest =
        new OneTimeTokenAuthenticationToken(input.getUsername(), input.getToken());
    return loginService.login(authenticationRequest);
  }
}
