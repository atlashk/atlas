package org.atlas.domain.auth.usecase.authentication.handler;

import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenOutput;

public interface RefreshTokenUseCase {

  RefreshTokenOutput handle(RefreshTokenInput input) throws Exception;
}
