package org.atlas.libs.kvstore.redis;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisKvStoreService implements KvStoreService {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void put(String storeName, String key, Object value) {
    String finalKey = buildKey(storeName, key);
    redisTemplate.opsForValue()
        .set(finalKey, value);
  }

  @Override
  public void put(String storeName, String key, Object value, Duration expiration) {
    String finalKey = buildKey(storeName, key);
    redisTemplate.opsForValue()
        .set(finalKey, value, expiration);
  }

  @Override
  public boolean putIfAbsent(String storeName, String key, Object value) {
    String finalKey = buildKey(storeName, key);
    return Boolean.TRUE.equals(redisTemplate.opsForValue()
            .setIfAbsent(finalKey, value));
  }

  @Override
  public boolean putIfAbsent(String storeName, String key, Object value, Duration expiration) {
    String finalKey = buildKey(storeName, key);
    return Boolean.TRUE.equals(redisTemplate.opsForValue()
            .setIfAbsent(finalKey, value, expiration));
  }

  @Override
  public <T> Optional<T> get(String storeName, String key, Class<T> clazz) {
    String finalKey = buildKey(storeName, key);
    Object value = redisTemplate.opsForValue()
        .get(finalKey);
    return Optional.ofNullable(JsonUtil.JSON_MAPPER.convertValue(value, clazz));
  }

  @Override
  public boolean exists(String storeName, String key) {
    String finalKey = buildKey(storeName, key);
    return redisTemplate.hasKey(finalKey);
  }

  @Override
  public boolean delete(String storeName, String key) {
    String finalKey = buildKey(storeName, key);
    return redisTemplate.delete(finalKey);
  }

  private String buildKey(String storeName, String key) {
    if (storeName != null && !storeName.isEmpty()) {
      return storeName + "::" + key;
    }
    return key;
  }
}
