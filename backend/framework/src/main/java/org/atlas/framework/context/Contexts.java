package org.atlas.framework.context;

import java.util.Date;
import java.util.Set;
import javax.annotation.Nullable;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.framework.error.AppError;
import org.atlas.framework.domain.exception.DomainException;

/**
 * Manages session info context for the current thread.
 */
public class Contexts {

  private static final ThreadLocal<ContextInfo> contextInfoThreadLocal = new ThreadLocal<>();

  @Nullable
  public static ContextInfo get() {
    return contextInfoThreadLocal.get();
  }

  public static String getSessionId() {
    return require().getSessionId();
  }

  public static Integer getUserId() {
    return require().getUserId();
  }

  public static Set<Role> getUserRoles() {
    return require().getUserRoles();
  }

  public static String getDeviceId() {
    return require().getDeviceId();
  }

  public static Date getExpiresAt() {
    return require().getExpiresAt();
  }

  /**
   * Require context info or throw UNAUTHORIZED error.
   */
  private static ContextInfo require() {
    ContextInfo context = get();
    if (context == null) {
      throw new DomainException(AppError.UNAUTHORIZED);
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
