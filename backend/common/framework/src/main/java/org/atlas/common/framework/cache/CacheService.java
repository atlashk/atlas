package org.atlas.common.framework.cache;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.kvstore.KvStoreService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(KvStoreService.class)
@RequiredArgsConstructor
@Slf4j
public class CacheService {

  private final KvStoreService kvStoreService;

  public void put(ApplicationCache cache, String key, Object value, long ttl) {
    kvStoreService.put(cache.getName(), key, value, Duration.ofSeconds(ttl));
  }

  public void put(ApplicationCache cache, String key, Object value) {
    put(cache, key, value, cache.getTtl());
  }

  public Optional<Object> get(ApplicationCache cache, String key) {
    return kvStoreService.get(cache.getName(), key);
  }

  public boolean evict(ApplicationCache cache, String key) {
    return kvStoreService.delete(cache.getName(), key);
  }
}
