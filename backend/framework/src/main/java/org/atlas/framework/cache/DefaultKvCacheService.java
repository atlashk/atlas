package org.atlas.framework.cache;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.kv.KvService;

@RequiredArgsConstructor
public class DefaultKvCacheService implements CacheService {

  private final KvService kvService;

  @Override
  public void put(String cacheName, String key, Object value, long ttl) {
    kvService.put(cacheName, key, value, Duration.ofSeconds(ttl));
  }

  @Override
  public Optional<Object> get(String cacheName, String key) {
    return kvService.get(cacheName, key);
  }

  @Override
  public boolean evict(String cacheName, String key) {
    return kvService.delete(cacheName, key);
  }
}
