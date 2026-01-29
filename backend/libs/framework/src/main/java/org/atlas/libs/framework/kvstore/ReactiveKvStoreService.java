package org.atlas.libs.framework.kvstore;

import java.time.Duration;
import reactor.core.publisher.Mono;

public interface ReactiveKvStoreService {

  Mono<Void> put(String storeName, String key, Object value);

  Mono<Void> put(String storeName, String key, Object value, Duration expiration);

  Mono<Boolean> putIfAbsent(String storeName, String key, Object value);

  Mono<Boolean> putIfAbsent(String storeName, String key, Object value, Duration expiration);

  Mono<Object> get(String storeName, String key);

  Mono<Boolean> exists(String storeName, String key);

  Mono<Boolean> delete(String storeName, String key);
}

