package org.atlas.framework.context;

import javax.annotation.Nullable;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;

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

  public static Role getUserRole() {
    return require().getUserRole();
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
