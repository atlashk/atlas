package org.atlas.infrastructure.lock.redisson;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.lock.LockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedissonLockService implements LockService {

  private final RedissonClient redissonClient;

  @Override
  public boolean acquireLock(String key, Duration waitTime, Duration leaseTime) {
    RLock lock = redissonClient.getLock(key);
    try {
      boolean acquiredLock = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(),
          TimeUnit.MILLISECONDS);
      if (acquiredLock) {
        log.info("Acquired lock for key {}", key);
        return true;
      } else {
        log.warn("Failed to acquire lock for key {}", key);
        return false;
      }
    } catch (InterruptedException e) {
      log.error("Interrupted while acquiring lock for key {}", key);
      Thread.currentThread().interrupt();
      return false;
    }
  }

  @Override
  public void releaseLock(String key) {
    RLock lock = redissonClient.getLock(key);
    lock.unlock();
    log.info("Released lock for key {}", key);
  }
}
