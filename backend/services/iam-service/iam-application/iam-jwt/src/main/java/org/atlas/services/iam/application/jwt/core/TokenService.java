package org.atlas.services.iam.application.jwt.core;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cryptography.RsaKeyLoader;
import org.atlas.libs.framework.jwt.DecodeJwtInput;
import org.atlas.libs.framework.jwt.EncodeJwtInput;
import org.atlas.libs.framework.jwt.Jwt;
import org.atlas.libs.framework.jwt.JwtUtil;
import org.atlas.libs.framework.security.SecurityConstant;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

  public String issueAccessToken(UserDetailsImpl userDetails, Date issuedAt, Date expiresAt)
      throws Exception {
    Jwt jwt = Jwt.builder()
        .issuer(SecurityConstant.TOKEN_ISSUER)
        .issuedAt(issuedAt)
        .subject(String.valueOf(userDetails.getId()))
        .audience(SecurityConstant.TOKEN_AUDIENCE)
        .expiresAt(expiresAt)
        .userRole(userDetails.getRole())
        .build();
    EncodeJwtInput input = EncodeJwtInput.builder()
        .jwt(jwt)
        .rsaPublicKey(RsaKeyLoader.loadPublicKey(SecurityConstant.RSA_PUBLIC_KEY_PATH))
        .rsaPrivateKey(RsaKeyLoader.loadPrivateKey(SecurityConstant.RSA_PRIVATE_KEY_PATH))
        .build();
    return JwtUtil.getInstance().encodeJwt(input);
  }

  public String issueRefreshToken(UserDetailsImpl userDetails, Date issuedAt, Date expiresAt)
      throws Exception {
    Jwt jwt = Jwt.builder()
        .issuer(SecurityConstant.TOKEN_ISSUER)
        .issuedAt(issuedAt)
        .subject(String.valueOf(userDetails.getId()))
        .audience(SecurityConstant.TOKEN_AUDIENCE)
        .expiresAt(expiresAt)
        .build();
    EncodeJwtInput input = EncodeJwtInput.builder()
        .jwt(jwt)
        .rsaPublicKey(RsaKeyLoader.loadPublicKey(SecurityConstant.RSA_PUBLIC_KEY_PATH))
        .rsaPrivateKey(RsaKeyLoader.loadPrivateKey(SecurityConstant.RSA_PRIVATE_KEY_PATH))
        .build();
    return JwtUtil.getInstance().encodeJwt(input);
  }

  public Jwt parseToken(String token) throws Exception {
    DecodeJwtInput input = DecodeJwtInput.builder()
        .jwt(token)
        .issuer(SecurityConstant.TOKEN_ISSUER)
        .rsaPublicKey(RsaKeyLoader.loadPublicKey(SecurityConstant.RSA_PUBLIC_KEY_PATH))
        .build();
    return JwtUtil.getInstance().decodeJwt(input);
  }
}
