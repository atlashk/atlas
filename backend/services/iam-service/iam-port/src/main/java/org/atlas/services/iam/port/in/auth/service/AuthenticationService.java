package org.atlas.services.iam.port.in.auth.service;

import java.util.Map;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenInput;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenOutput;
import org.atlas.services.iam.port.in.auth.model.LoginInput;
import org.atlas.services.iam.port.in.auth.model.LoginOutput;
import org.atlas.services.iam.port.in.auth.model.OneTimeTokenLoginInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenOutput;

public interface AuthenticationService {

  Map<String, Object> jwkSet();

  LoginOutput login(LoginInput input) throws Exception;

  RefreshTokenOutput refreshToken(RefreshTokenInput input) throws Exception;

  void logout(String accessToken) throws Exception;

  LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception;
  
  GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input);
}
