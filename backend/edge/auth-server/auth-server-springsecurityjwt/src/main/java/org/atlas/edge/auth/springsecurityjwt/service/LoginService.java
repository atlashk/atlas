package org.atlas.edge.auth.springsecurityjwt.service;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.edge.auth.springsecurityjwt.core.UserDetailsImpl;
import org.atlas.framework.security.SecurityConstant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  public LoginOutput login(Authentication authenticationRequest) throws Exception {
    Authentication authentication = authenticationManager.authenticate(authenticationRequest);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    // Issue refresh token
    Date now = new Date();
    Date refreshTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME * 1000);
    String refreshToken = tokenService.issueRefreshToken(userDetails, now, refreshTokenExpiresAt);

    // Issue access token
    now = new Date();
    Date accessTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME * 1000);
    String accessToken = tokenService.issueAccessToken(userDetails, now, accessTokenExpiresAt);

    return new LoginOutput(accessToken, refreshToken);
  }
}
