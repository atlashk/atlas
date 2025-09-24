package org.atlas.framework.cache;

import java.util.Optional;

public interface CachePort {

  void put(String key, Object value, long ttl);

  Optional<Object> get(String key);
}
