package org.atlas.framework.cache;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.kvstore.KvStoreService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

  private final KvStoreService kvStoreService;

  public void put(ApplicationCache cache, String key, Object value) {
    // The default TTL is 1 hour
    put(cache, key, value, 360);
  }

  public void put(ApplicationCache cache, String key, Object value, long ttl) {
    kvStoreService.put(cache.getName(), key, value, Duration.ofSeconds(ttl));
    log.info("Cache put: {}:{}={}", cache.getName(), key, value);
  }

  public Optional<Object> get(ApplicationCache cache, String key) {
    Optional<Object> value = kvStoreService.get(cache.getName(), key);
    if (value.isPresent()) {
      log.info("Cache hit: {}:{}={}", cache.getName(), key, value.get());
    } else {
      log.info("Cache miss: {}:{}", cache.getName(), key);
    }
    return value;
  }

  public boolean evict(ApplicationCache cache, String key) {
    boolean deleted = kvStoreService.delete(cache.getName(), key);
    if (deleted) {
      log.info("Cache evict: {}:{}", cache.getName(), key);
    }
    return deleted;
  }
}
