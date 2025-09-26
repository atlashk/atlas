package org.atlas.framework.cache;

import java.util.Optional;

public interface CachePort {

  void put(String cacheName, String key, Object value, long ttl);

  default void put(Caches cache, String key, Object value) {
    put(cache.getName(), key, value, cache.getTtl());
  }

  Optional<Object> get(String cacheName, String key);

  boolean invalidate(String cacheName, String key);
}
