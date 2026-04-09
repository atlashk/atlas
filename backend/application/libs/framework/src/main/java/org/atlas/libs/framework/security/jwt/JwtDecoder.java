package org.atlas.libs.framework.security.jwt;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.security.Principal;

@UtilityClass
public class JwtDecoder {

  private static final Pattern KEYCLOAK_PATTERN = Pattern.compile("^https?://.*/realms/.*$");

  public static Principal decode(String jwt) {
    String issuer = JwtUtil.extractIssuer(jwt);
    if (issuer != null) {
      if (KEYCLOAK_PATTERN.matcher(issuer).matches()) {
        return decodeKeycloakJwt(jwt);
      }
    }
    return decodeDefaultJwt(jwt);
  }

  private static Principal decodeDefaultJwt(String jwt) {
    Principal principal = new Principal();
    principal.setAccessToken(jwt);

    // userId
    principal.setUserId(JwtUtil.extractSubject(jwt));

    // userRole
    JwtUtil.<UserRole>extractClaim(jwt, Claim.USER_ROLE)
        .ifPresent(principal::setUserRole);

    // firstName
    JwtUtil.<String>extractClaim(jwt, Claim.FIRST_NAME)
        .ifPresent(principal::setFirstName);

    // lastName
    JwtUtil.<String>extractClaim(jwt, Claim.LAST_NAME)
        .ifPresent(principal::setLastName);

    // email
    JwtUtil.<String>extractClaim(jwt, Claim.EMAIL)
        .ifPresent(principal::setEmail);

    // phone
    JwtUtil.<String>extractClaim(jwt, Claim.PHONE_NUMBER)
        .ifPresent(principal::setPhoneNumber);

    return principal;
  }

  @SuppressWarnings("unchecked")
  private static Principal decodeKeycloakJwt(String jwt) {
    Principal principal = new Principal();
    principal.setAccessToken(jwt);

    // userId
    JwtUtil.<String>extractClaim(jwt, Claim.PREFERRED_USERNAME)
        .ifPresent(preferredUsername -> principal.setUserId(preferredUsername.toUpperCase()));

    // userRole
    JwtUtil.extractClaim(jwt, "realm_access", Map.class)
        .ifPresent(realmAccess -> {
          Object rolesObj = realmAccess.get("roles");
          if (rolesObj instanceof List) {
            List<String> roles = (List<String>) rolesObj;
            if (roles.contains("admin")) {
              principal.setUserRole(UserRole.ADMIN);
            } else if (roles.contains("user")) {
              principal.setUserRole(UserRole.USER);
            }
          }
        });

    // firstName
    JwtUtil.<String>extractClaim(jwt, Claim.GIVEN_NAME)
        .ifPresent(principal::setFirstName);

    // lastName
    JwtUtil.<String>extractClaim(jwt, Claim.FAMILY_NAME)
        .ifPresent(principal::setLastName);

    // email
    JwtUtil.<String>extractClaim(jwt, Claim.EMAIL)
        .ifPresent(principal::setEmail);

    // phone
    JwtUtil.<String>extractClaim(jwt, Claim.PHONE_NUMBER)
        .ifPresent(principal::setPhoneNumber);

    return principal;
  }
}
