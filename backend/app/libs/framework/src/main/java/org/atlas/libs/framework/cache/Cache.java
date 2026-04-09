package org.atlas.libs.framework.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * Cache annotation for method-level caching.
 * <p>
 * This annotation can be applied to methods to enable caching functionality. The cached data will
 * be stored using the specified key and will expire after the specified time-to-live (TTL)
 * duration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

  String DEFAULT_KEY = "__default__";

  /**
   * @return cache name
   */
  String name();

  /**
   * @return cache key
   */
  String key() default DEFAULT_KEY;

  /**
   * Time-to-live (TTL) for the cached value in seconds. After this duration, the cached value will
   * expire and be removed. Default value is not set.
   *
   * @return the TTL in seconds
   */
  long ttl() default 0L;
}
