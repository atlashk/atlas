package org.atlas.framework.cache;

import java.util.Optional;

public interface CacheService {

  void put(ApplicationCache cache, String key, Object value, long ttl);

  default void put(ApplicationCache cache, String key, Object value) {
    put(cache, key, value, cache.getTtl());
  }

  Optional<Object> get(ApplicationCache cache, String key);

  boolean evict(ApplicationCache cache, String key);
}
