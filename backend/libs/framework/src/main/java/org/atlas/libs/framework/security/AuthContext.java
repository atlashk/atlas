package org.atlas.libs.framework.security;

import jakarta.annotation.Nullable;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;

/**
 * Manages session info context for the current thread.
 */
public class AuthContext {

  private static final ThreadLocal<Principal> principalThreadLocal = new ThreadLocal<>();

  @Nullable
  public static Principal getPrincipal() {
    return principalThreadLocal.get();
  }

  /**
   * Require context info or throw UNAUTHORIZED error.
   */
  public static Principal requirePrincipal() {
    Principal context = getPrincipal();
    if (context == null) {
      throw new BaseDomainException(CommonDomainError.UNAUTHORIZED);
    }
    return context;
  }

  /**
   * Set context for the current thread.
   */
  public static void setPrincipal(Principal principal) {
    principalThreadLocal.set(principal);
  }

  /**
   * Clear context after request completes to avoid memory leaks.
   */
  public static void clear() {
    principalThreadLocal.remove();
  }
}
