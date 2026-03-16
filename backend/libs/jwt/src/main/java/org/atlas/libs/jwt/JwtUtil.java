package org.atlas.libs.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.cryptography.RsaKeyLoader;
import org.atlas.libs.framework.security.CustomClaim;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.util.LegacyDateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.uuid.UUIDGenerator;

@UtilityClass
public class JwtUtil {

  private static final String BEARER_PREFIX = "Bearer ";

  // Relative path to resources folder
  private static final String RSA_PUBLIC_KEY_PATH = "secret/token.pub";
  private static final String RSA_PRIVATE_KEY_PATH = "secret/token.key";

  public static Map<String, Object> jwkSet() throws IOException, InvalidKeySpecException {
    RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey(RSA_PUBLIC_KEY_PATH);
    RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
        .keyID(SecurityConstant.JWKS_KEY_ID)
        .build();
    return new JWKSet(rsaKey).toJSONObject();
  }

  public static String issueAccessToken(IssueTokenInput input)
      throws Exception {
    Date issuedAt = LegacyDateUtil.now();
    Date expiresAt = new Date(
        issuedAt.getTime() + SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME * 1000);

    JWTCreator.Builder builder = JWT.create()
        .withJWTId(UUIDGenerator.generate())
        .withIssuer(SecurityConstant.TOKEN_ISSUER)
        .withIssuedAt(issuedAt)
        .withSubject(input.getUserId())
        .withAudience(SecurityConstant.TOKEN_AUDIENCE)
        .withExpiresAt(expiresAt);

    // Custom claims
    builder.withClaim(CustomClaim.USER_ROLE.getClaimName(), input.getRole().name());

    // Signing
    RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey(RSA_PUBLIC_KEY_PATH);
    RSAPrivateKey rsaPrivateKey = RsaKeyLoader.loadPrivateKey(RSA_PRIVATE_KEY_PATH);
    Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
    return builder.sign(algorithm);
  }

  public static String issueRefreshToken(IssueTokenInput input) throws Exception {
    Date issuedAt = LegacyDateUtil.now();
    Date expiresAt = new Date(
        issuedAt.getTime() + SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME * 1000);

    JWTCreator.Builder builder = JWT.create()
        .withJWTId(UUIDGenerator.generate())
        .withIssuer(SecurityConstant.TOKEN_ISSUER)
        .withIssuedAt(issuedAt)
        .withSubject(input.getUserId())
        .withAudience(SecurityConstant.TOKEN_AUDIENCE)
        .withExpiresAt(expiresAt);

    // Signing
    RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey(RSA_PUBLIC_KEY_PATH);
    RSAPrivateKey rsaPrivateKey = RsaKeyLoader.loadPrivateKey(RSA_PRIVATE_KEY_PATH);
    Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
    return builder.sign(algorithm);
  }

  public static String extractBearerToken(String authorizationHeader) {
    if (StringUtil.isBlank(authorizationHeader)) return null;
    return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
  }

  public static String extractSubject(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getSubject();
  }

  public static Date extractExpiresAt(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    return decodedJWT.getExpiresAt();
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<T> extractClaim(String token, CustomClaim claim) {
    DecodedJWT decodedJWT = JWT.decode(token);
    Claim extractedClaim = decodedJWT.getClaim(claim.getClaimName());
    return Optional.ofNullable((T) extractedClaim.as(claim.getClazz()));
  }
}
