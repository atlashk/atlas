package org.atlas.edge.auth.springsecurityjwt.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.LoginUseCase;
import org.atlas.domain.auth.usecase.authentication.model.LoginInput;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.edge.auth.springsecurityjwt.service.LoginService;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@UseCaseHandler
@RequiredArgsConstructor
public class LoginUseCaseHandler implements LoginUseCase {

  private final LoginService loginService;

  @Override
  public LoginOutput handle(LoginInput input) throws Exception {
    UsernamePasswordAuthenticationToken authenticationRequest =
        new UsernamePasswordAuthenticationToken(input.getUsername(), input.getPassword());
    return loginService.login(authenticationRequest);
  }
}
