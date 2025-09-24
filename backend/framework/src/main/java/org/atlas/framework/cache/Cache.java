package org.atlas.framework.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache annotation for method-level caching.
 * 
 * This annotation can be applied to methods to enable caching functionality.
 * The cached data will be stored using the specified key and will expire after
 * the specified time-to-live (TTL) duration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    /**
     * The cache key used to store and retrieve the cached value.
     * Supports SpEL (Spring Expression Language) expressions.
     * 
     * @return the cache key
     */
    String key();

    /**
     * Time-to-live (TTL) for the cached value in seconds.
     * After this duration, the cached value will expire and be removed.
     * Default value is 300 seconds (5 minutes).
     * 
     * @return the TTL in seconds
     */
    long ttl() default 300;
}
