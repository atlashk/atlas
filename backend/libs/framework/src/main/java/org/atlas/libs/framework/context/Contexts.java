package org.atlas.libs.framework.context;

import jakarta.annotation.Nullable;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.user.UserRole;

/**
 * Manages session info context for the current thread.
 */
public class Contexts {

  private static final ThreadLocal<ContextInfo> contextInfoThreadLocal = new ThreadLocal<>();

  @Nullable
  public static ContextInfo get() {
    return contextInfoThreadLocal.get();
  }

  public static Integer getUserId() {
    return require().getUserId();
  }

  public static UserRole getUserRole() {
    return require().getUserRole();
  }

  public static String getUserInfo() {
    ContextInfo contextInfo = get();
    if (contextInfo == null) {
      return "anonymous";
    }
    Integer userId = contextInfo.getUserId();
    if (userId == null) {
      return contextInfo.getIpAddress();
    }
    return userId.toString();
  }

  /**
   * Require context info or throw UNAUTHORIZED error.
   */
  private static ContextInfo require() {
    ContextInfo context = get();
    if (context == null) {
      throw new DomainException(DomainError.UNAUTHORIZED);
    }
    return context;
  }

  /**
   * Set context for the current thread.
   */
  public static void set(ContextInfo contextInfo) {
    contextInfoThreadLocal.set(contextInfo);
  }

  /**
   * Clear context after request completes to avoid memory leaks.
   */
  public static void clear() {
    contextInfoThreadLocal.remove();
  }
}
