package org.atlas.edge.auth.springsecurityjwt.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.GenerateOneTimeTokenUseCase;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;

@UseCaseHandler
@RequiredArgsConstructor
public class GenerateOneTimeTokenUseCaseHandler implements GenerateOneTimeTokenUseCase {

  private final OneTimeTokenService oneTimeTokenService;

  @Override
  public GenerateOneTimeTokenOutput handle(GenerateOneTimeTokenInput input) {
    OneTimeToken token = oneTimeTokenService.generate(
        new GenerateOneTimeTokenRequest(input.getUsername()));
    return new GenerateOneTimeTokenOutput(token.getTokenValue());
  }
}
