package org.atlas.domain.auth.usecase.authentication.handler;

import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.domain.auth.usecase.authentication.model.OneTimeTokenLoginInput;

public interface OneTimeTokenLoginUseCase {

  LoginOutput handle(OneTimeTokenLoginInput input) throws Exception;
}
