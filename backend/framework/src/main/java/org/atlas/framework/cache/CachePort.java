package org.atlas.framework.cache;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

  void set(String key, Object value);
  void set(String cacheName, String key, Object value);
  void set(String key, Object value, Duration timeout);
  void set(String cacheName, String key, Object value, Duration timeout);
  Optional<Object> get(String key);
  Optional<Object> get(String cacheName, String key);
  Boolean delete(String key);
  Boolean delete(String cacheName, String key);
}
