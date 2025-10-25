package org.atlas.framework.cache;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.kv.KvService;

@RequiredArgsConstructor
public class DefaultKvCacheService implements CacheService {

  private final KvService kvService;

  @Override
  public void put(ApplicationCache cache, String key, Object value, long ttl) {
    kvService.put(cache.getName(), key, value, Duration.ofSeconds(ttl));
  }

  @Override
  public Optional<Object> get(ApplicationCache cache, String key) {
    return kvService.get(cache.getName(), key);
  }

  @Override
  public boolean evict(ApplicationCache cache, String key) {
    return kvService.delete(cache.getName(), key);
  }
}
