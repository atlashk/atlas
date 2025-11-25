package org.atlas.edge.auth.springsecurityjwt.usecase.authentication.handler;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.User;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.domain.auth.usecase.authentication.handler.RefreshTokenUseCase;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenOutput;
import org.atlas.edge.auth.springsecurityjwt.service.TokenService;
import org.atlas.edge.auth.springsecurityjwt.core.UserDetailsImpl;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.jwt.Jwt;
import org.atlas.framework.security.SecurityConstant;

@UseCaseHandler
@RequiredArgsConstructor
public class RefreshTokenUseCaseHandler implements RefreshTokenUseCase {

  private final UserRepository userRepository;
  private final TokenService tokenService;

  public RefreshTokenOutput handle(RefreshTokenInput input) throws Exception {
    // Parse refresh token
    Jwt refreshTokenJwt;
    try {
      refreshTokenJwt = tokenService.parseToken(input.getRefreshToken());
    } catch (Exception e) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Invalid refresh token");
    }

    // Reissue tokens
    User user = userRepository.findById(refreshTokenJwt.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    UserDetailsImpl userDetails = new UserDetailsImpl(user);

    // Issue new access token
    Date now = new Date();
    Date accessTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME * 1000);
    String accessToken = tokenService.issueAccessToken(userDetails, now, accessTokenExpiresAt);

    // Issue new refresh token
    now = new Date();
    Date refreshTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME * 1000);
    String refreshToken = tokenService.issueRefreshToken(userDetails, now, refreshTokenExpiresAt);

    return new RefreshTokenOutput(accessToken, refreshToken);
  }
}
