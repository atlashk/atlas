package org.atlas.libs.kvstore.redis.reactive;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.kvstore.ReactiveKvStoreService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisReactiveKvStoreService implements ReactiveKvStoreService {

  private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

  @Override
  public Mono<Void> put(String storeName, String key, Object value) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.opsForValue().set(finalKey, value).then();
  }

  @Override
  public Mono<Void> put(String storeName, String key, Object value, Duration expiration) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.opsForValue().set(finalKey, value, expiration).then();
  }

  @Override
  public Mono<Boolean> putIfAbsent(String storeName, String key, Object value) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.opsForValue().setIfAbsent(finalKey, value)
        .map(Boolean::booleanValue);
  }

  @Override
  public Mono<Boolean> putIfAbsent(String storeName, String key, Object value,
      Duration expiration) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.opsForValue().setIfAbsent(finalKey, value, expiration)
        .map(Boolean::booleanValue);
  }

  @Override
  public Mono<Object> get(String storeName, String key) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.opsForValue().get(finalKey);
  }

  @Override
  public Mono<Boolean> exists(String storeName, String key) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.hasKey(finalKey);
  }

  @Override
  public Mono<Boolean> delete(String storeName, String key) {
    String finalKey = buildKey(storeName, key);
    return reactiveRedisTemplate.delete(finalKey).map(count -> count != null && count > 0);
  }

  private String buildKey(String storeName, String key) {
    if (storeName != null && !storeName.isEmpty()) {
      return storeName + "::" + key;
    }
    return key;
  }
}
