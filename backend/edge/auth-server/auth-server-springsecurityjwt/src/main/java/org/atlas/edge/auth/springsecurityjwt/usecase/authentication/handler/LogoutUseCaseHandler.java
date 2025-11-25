package org.atlas.edge.auth.springsecurityjwt.usecase.authentication.handler;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.LogoutUseCase;
import org.atlas.edge.auth.springsecurityjwt.service.TokenService;
import org.atlas.framework.cryptography.HashingUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.jwt.Jwt;
import org.atlas.framework.kvstore.KvStoreService;
import org.atlas.framework.security.SecurityConstant;
import org.atlas.framework.util.DateUtil;
import org.atlas.framework.util.StringUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class LogoutUseCaseHandler implements LogoutUseCase {

  private final KvStoreService kvStoreService;
  private final TokenService tokenService;

  @Override
  public void handle(String accessToken) throws Exception {
    if (StringUtil.isBlank(accessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Missing access token");
    }

    String hashedAccessToken = HashingUtil.sha256ToHex(accessToken);
    if (kvStoreService.exists(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME,
        hashedAccessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED,
          "Access token has been already inactivated");
    }

    Jwt jwt = tokenService.parseToken(accessToken);
    long now = DateUtil.timestamp();
    long ttlMs = Math.max(1000L, jwt.getExpiresAt().getTime() - now);
    kvStoreService.put(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME, hashedAccessToken, "1",
        Duration.ofMillis(ttlMs));
  }
}
