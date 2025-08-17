package org.atlas.infrastructure.cache.redis;

import static org.atlas.infrastructure.cache.redis.config.RedisCacheConfig.DEFAULT_CACHE_NAME;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.cache.CachePort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void set(String key, Object value) {
    this.set(DEFAULT_CACHE_NAME, key, value);
  }

  @Override
  public void set(String cacheName, String key, Object value) {
    redisTemplate.opsForValue()
        .set(buildKey(cacheName, key), value);
  }

  @Override
  public void set(String key, Object value, Duration timeout) {
    this.set(DEFAULT_CACHE_NAME, key, value, timeout);
  }

  @Override
  public void set(String cacheName, String key, Object value, Duration timeout) {
    redisTemplate.opsForValue()
        .set(buildKey(cacheName, key), value, timeout);
  }

  @Override
  public Optional<Object> get(String key) {
    return get(DEFAULT_CACHE_NAME, key);
  }

  @Override
  public Optional<Object> get(String cacheName, String key) {
    return Optional.ofNullable(redisTemplate.opsForValue()
        .get(buildKey(cacheName, key)));
  }

  @Override
  public Boolean delete(String key) {
    return delete(DEFAULT_CACHE_NAME, key);
  }

  @Override
  public Boolean delete(String cacheName, String key) {
    return redisTemplate.delete(buildKey(cacheName, key));
  }

  // Helper method to build key with cache name
  private String buildKey(String cacheName, String key) {
    return cacheName + "::" + key;
  }
}
