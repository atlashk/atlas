package org.atlas.edge.auth.keycloak.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.GenerateOneTimeTokenUseCase;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;

@UseCaseHandler
@RequiredArgsConstructor
public class GenerateOneTimeTokenUseCaseHandler implements GenerateOneTimeTokenUseCase {

  @Override
  public GenerateOneTimeTokenOutput handle(GenerateOneTimeTokenInput input) {
    throw new DomainException(DomainError.BAD_REQUEST, "One-time token is not supported");
  }
}
