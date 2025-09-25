package org.atlas.framework.cache;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.kv.KvPort;

@RequiredArgsConstructor
public class DefaultKvCacheAdapter implements CachePort {

  private final KvPort kvPort;

  @Override
  public void put(String cacheName, String key, Object value, long ttl) {
    kvPort.put(cacheName, key, value, Duration.ofSeconds(ttl));
  }

  @Override
  public Optional<Object> get(String cacheName, String key) {
    return kvPort.get(cacheName, key);
  }
}
