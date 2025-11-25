package org.atlas.domain.auth.service;

import org.atlas.domain.auth.model.GenerateOneTimeTokenRequest;
import org.atlas.domain.auth.model.GenerateOneTimeTokenResponse;
import org.atlas.domain.auth.model.LoginRequest;
import org.atlas.domain.auth.model.LoginResponse;
import org.atlas.domain.auth.model.OneTimeTokenLoginRequest;
import org.atlas.domain.auth.model.RefreshTokenRequest;
import org.atlas.domain.auth.model.RefreshTokenResponse;

public interface AuthService {

  LoginResponse login(LoginRequest request) throws Exception;

  LoginResponse oneTimeTokenLogin(OneTimeTokenLoginRequest request) throws Exception;

  GenerateOneTimeTokenResponse generateOneTimeToken(GenerateOneTimeTokenRequest request);

  RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws Exception;

  void logout();
}
