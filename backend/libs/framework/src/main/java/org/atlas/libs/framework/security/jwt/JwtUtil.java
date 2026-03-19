package org.atlas.libs.framework.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.security.cryptography.RsaKeyLoader;
import org.atlas.libs.framework.util.LegacyDateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.uuid.UUIDGenerator;

@UtilityClass
public class JwtUtil {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String RSA_PUBLIC_KEY_PATH = "token/token.pub";
  private static final String RSA_PRIVATE_KEY_PATH = "token/token.key";

  public static Map<String, Object> jwkSet() throws IOException, InvalidKeySpecException {
    RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey(RSA_PUBLIC_KEY_PATH);
    RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
        .keyID(SecurityConstant.JWKS_KEY_ID)
        .build();
    return new JWKSet(rsaKey).toJSONObject();
  }

  public static String issueAccessToken(Principal principal) throws Exception {
    return issueToken(principal, SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME, true);
  }

  public static String issueRefreshToken(Principal principal) throws Exception {
    return issueToken(principal, SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME, false);
  }

  public static String extractBearerToken(String authorizationHeader) {
    if (StringUtil.isBlank(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
      return null;
    }
    return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
  }

  public static String extractIssuer(String token) {
    return extractClaims(token).getIssuer();
  }

  public static String extractSubject(String token) {
    return extractClaims(token).getSubject();
  }

  public static String extractSubjectVerified(String token) {
    return extractClaimsVerified(token).getSubject();
  }

  public static Date extractExpiresAt(String token) {
    return extractClaims(token).getExpirationTime();
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<T> extractClaim(String token, String claimName, Class<T> clazz) {
    JWTClaimsSet claimsSet = extractClaims(token);
    Object value = claimsSet.getClaim(claimName);
    if (value == null) {
      return Optional.empty();
    }

    if (clazz.isEnum()) {
      String enumName = String.valueOf(value);
      try {
        return Optional.of(
            (T) Enum.valueOf((Class<Enum>) clazz.asSubclass(Enum.class), enumName));
      } catch (IllegalArgumentException ignored) {
        return Optional.empty();
      }
    }

    if (clazz.isInstance(value)) {
      return Optional.of((T) value);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<T> extractClaim(String token, Claim claim) {
    return extractClaim(token, claim.getClaimName(), (Class<T>) claim.getClazz());
  }

  private static String issueToken(Principal principal, long expirationTimeSeconds,
      boolean includeCustomClaims)
      throws IOException, InvalidKeySpecException, JOSEException {
    Date issuedAt = LegacyDateUtil.now();
    Date expiresAt = new Date(issuedAt.getTime() + expirationTimeSeconds * 1000);
    JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
        .jwtID(UUIDGenerator.generate())
        .issuer(SecurityConstant.TOKEN_ISSUER)
        .issueTime(issuedAt)
        .subject(principal.getUserId())
        .audience(SecurityConstant.TOKEN_AUDIENCE)
        .expirationTime(expiresAt);

    if (includeCustomClaims) {
      putClaimIfNotBlank(claimsBuilder, Claim.FIRST_NAME, principal.getFirstName());
      putClaimIfNotBlank(claimsBuilder, Claim.LAST_NAME, principal.getLastName());
      putClaimIfNotBlank(claimsBuilder, Claim.EMAIL, principal.getEmail());
      putClaimIfNotBlank(claimsBuilder, Claim.PHONE_NUMBER, principal.getPhoneNumber());
      if (principal.getUserRole() != null) {
        claimsBuilder.claim(Claim.USER_ROLE.getClaimName(), principal.getUserRole().name());
      }
    }

    return sign(claimsBuilder.build());
  }

  private static void putClaimIfNotBlank(
      JWTClaimsSet.Builder claimsBuilder, Claim claim, String value) {
    if (StringUtil.isNotBlank(value)) {
      claimsBuilder.claim(claim.getClaimName(), value);
    }
  }

  private static String sign(JWTClaimsSet claimsSet)
      throws IOException, InvalidKeySpecException, JOSEException {
    RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey(RSA_PUBLIC_KEY_PATH);
    RSAPrivateKey rsaPrivateKey = RsaKeyLoader.loadPrivateKey(RSA_PRIVATE_KEY_PATH);
    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(SecurityConstant.JWKS_KEY_ID)
            .build(),
        claimsSet);
    signedJWT.sign(
        new RSASSASigner(new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build()));
    return signedJWT.serialize();
  }

  private static JWTClaimsSet extractClaims(String token) {
    try {
      return SignedJWT.parse(token).getJWTClaimsSet();
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    }
  }

  private static JWTClaimsSet extractClaimsVerified(String token) {
    try {
      SignedJWT signedJwt = SignedJWT.parse(token);
      RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey(RSA_PUBLIC_KEY_PATH);
      if (!signedJwt.verify(new RSASSAVerifier(rsaPublicKey))) {
        throw new IllegalArgumentException("Invalid JWT signature");
      }
      JWTClaimsSet claimsSet = signedJwt.getJWTClaimsSet();
      Date expiresAt = claimsSet.getExpirationTime();
      if (expiresAt == null || expiresAt.before(LegacyDateUtil.now())) {
        throw new IllegalArgumentException("JWT token expired");
      }
      return claimsSet;
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid JWT token", e);
    } catch (IOException | InvalidKeySpecException | JOSEException e) {
      throw new IllegalArgumentException("Could not verify JWT token", e);
    }
  }
}
