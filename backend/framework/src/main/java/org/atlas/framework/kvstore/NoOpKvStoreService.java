package org.atlas.framework.kvstore;

import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(KvStoreService.class)
public class NoOpKvStoreService implements KvStoreService {

  @Override
  public void put(String storeName, String key, Object value) {

  }

  @Override
  public void put(String storeName, String key, Object value, Duration expiration) {

  }

  @Override
  public boolean putIfAbsent(String storeName, String key, Object value) {
    return false;
  }

  @Override
  public boolean putIfAbsent(String storeName, String key, Object value, Duration expiration) {
    return false;
  }

  @Override
  public Optional<Object> get(String storeName, String key) {
    return Optional.empty();
  }

  @Override
  public boolean exists(String storeName, String key) {
    return false;
  }

  @Override
  public boolean delete(String storeName, String key) {
    return false;
  }
}
