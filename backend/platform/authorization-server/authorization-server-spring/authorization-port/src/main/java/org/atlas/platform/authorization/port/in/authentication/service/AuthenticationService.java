package org.atlas.platform.authorization.port.in.authentication.service;

import org.atlas.platform.authorization.port.in.authentication.model.ChangePasswordInput;
import org.atlas.platform.authorization.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.platform.authorization.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.platform.authorization.port.in.authentication.model.LoginInput;
import org.atlas.platform.authorization.port.in.authentication.model.LoginOutput;
import org.atlas.platform.authorization.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.platform.authorization.port.in.authentication.model.RefreshTokenInput;
import org.atlas.platform.authorization.port.in.authentication.model.RefreshTokenOutput;

public interface AuthenticationService {

  LoginOutput login(LoginInput input) throws Exception;

  RefreshTokenOutput refreshToken(RefreshTokenInput input) throws Exception;

  void logout(String accessToken) throws Exception;

  void changePassword(ChangePasswordInput input);

  LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception;
  
  GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input);
}
