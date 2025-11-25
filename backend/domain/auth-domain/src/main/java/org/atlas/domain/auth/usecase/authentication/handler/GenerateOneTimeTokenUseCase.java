package org.atlas.domain.auth.usecase.authentication.handler;

import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenOutput;

public interface GenerateOneTimeTokenUseCase {

  GenerateOneTimeTokenOutput handle(GenerateOneTimeTokenInput input);
}
