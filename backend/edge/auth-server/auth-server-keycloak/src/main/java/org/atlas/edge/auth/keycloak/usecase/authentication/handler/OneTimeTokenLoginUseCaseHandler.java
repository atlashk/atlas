package org.atlas.edge.auth.keycloak.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.OneTimeTokenLoginUseCase;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.domain.auth.usecase.authentication.model.OneTimeTokenLoginInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;

@UseCaseHandler
@RequiredArgsConstructor
public class OneTimeTokenLoginUseCaseHandler implements OneTimeTokenLoginUseCase {

  @Override
  public LoginOutput handle(OneTimeTokenLoginInput input) throws Exception {
    throw new DomainException(DomainError.BAD_REQUEST, "One-time token login is not supported");
  }
}
