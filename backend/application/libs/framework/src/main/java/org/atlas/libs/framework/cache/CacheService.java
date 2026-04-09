package org.atlas.libs.framework.cache;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.util.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(KvStoreService.class)
@RequiredArgsConstructor
@Slf4j
public class CacheService {

  private final KvStoreService kvStoreService;

  public void put(String cache, String key, Object value) {
    kvStoreService.put(cache, key, value);
  }

  public void put(String cache, String key, Object value, long ttl) {
    kvStoreService.put(cache, key, value, Duration.ofSeconds(ttl));
  }

  public <T> Optional<T> get(String cache, String key, Class<T> clazz) {
    return kvStoreService.get(cache, key, clazz);
  }

  public <T> Optional<List<T>> getList(String cache, String key, Class<T> elementClass) {
    return kvStoreService.get(cache, key, Object.class)
        .map(value -> JsonUtil.JSON_MAPPER.convertValue(value,
            JsonUtil.JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass)
        ));
  }

  public boolean evict(String cache, String key) {
    return kvStoreService.delete(cache, key);
  }
}
