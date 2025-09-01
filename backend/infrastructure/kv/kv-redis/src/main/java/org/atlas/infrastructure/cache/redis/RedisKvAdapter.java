package org.atlas.infrastructure.cache.redis;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.kv.KvPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisKvAdapter implements KvPort {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void put(String key, Object value) {
    redisTemplate.opsForValue().set(key, value);
  }

  @Override
  public void put(String key, Object value, Duration timeout) {
    redisTemplate.opsForValue().set(key, value, timeout);
  }

  @Override
  public boolean putIfAbsent(String key, Object value) {
    return redisTemplate.opsForValue().setIfAbsent(key, value);
  }

  @Override
  public boolean putIfAbsent(String key, Object value, Duration timeout) {
    return redisTemplate.opsForValue().setIfAbsent(key, value, timeout);
  }

  @Override
  public Optional<Object> get(String key) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  @Override
  public boolean exists(String key) {
    return redisTemplate.hasKey(key);
  }

  @Override
  public boolean delete(String key) {
    return redisTemplate.delete(key);
  }
}
