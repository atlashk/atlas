package org.atlas.domain.auth.usecase.authentication.handler;

import org.atlas.domain.auth.usecase.authentication.model.LoginInput;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;

public interface LoginUseCase {

  LoginOutput handle(LoginInput input) throws Exception;
}
