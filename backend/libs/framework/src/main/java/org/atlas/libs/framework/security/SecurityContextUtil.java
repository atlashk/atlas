package org.atlas.libs.framework.security;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityContextUtil {

  public static Principal requirePrincipal() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(authentication -> (Principal) authentication.getPrincipal())
        .orElseThrow(() -> new BaseDomainException(CommonDomainError.UNAUTHORIZED));
  }

  public static void setContext(Principal principal) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        principal, null, principal.getAuthorities());
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
