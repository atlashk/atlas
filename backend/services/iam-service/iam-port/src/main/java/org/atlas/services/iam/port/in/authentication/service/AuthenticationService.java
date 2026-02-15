package org.atlas.services.iam.port.in.authentication.service;

import java.util.Map;
import org.atlas.services.iam.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.services.iam.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.services.iam.port.in.authentication.model.LoginInput;
import org.atlas.services.iam.port.in.authentication.model.LoginOutput;
import org.atlas.services.iam.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.services.iam.port.in.authentication.model.RefreshTokenInput;
import org.atlas.services.iam.port.in.authentication.model.RefreshTokenOutput;
import org.atlas.services.iam.port.in.authentication.model.ChangePasswordInput;

public interface AuthenticationService {

  Map<String, Object> jwkSet() throws Exception;

  LoginOutput login(LoginInput input) throws Exception;

  RefreshTokenOutput refreshToken(RefreshTokenInput input) throws Exception;

  void logout(String accessToken) throws Exception;

  void changePassword(ChangePasswordInput input);

  String resetPassword(String userId);

  LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception;
  
  GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input);
}
