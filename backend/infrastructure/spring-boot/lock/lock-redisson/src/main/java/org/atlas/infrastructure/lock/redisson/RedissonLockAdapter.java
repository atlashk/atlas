package org.atlas.infrastructure.lock.redisson;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockPort;
import org.atlas.framework.resilience.RetryUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedissonLockAdapter implements LockPort {

  private final RedissonClient redissonClient;

  /**
   * Executes the given task under a distributed lock.
   *
   * @param task    the logic to be executed once the lock is acquired
   * @param key     the Redis key to use for the lock
   * @param timeout how long the lock is held before auto-release
   * @throws LockAcquisitionException if the lock cannot be acquired
   */
  @Override
  public void doWithLock(Runnable task, String key, Duration timeout)
      throws LockAcquisitionException {
    RetryUtil.retryOn(() -> {
      RLock lock = redissonClient.getLock(key);
      boolean acquired = false;
      try {
        // Try to acquire the lock immediately, for up to timeout
        acquired = lock.tryLock(0, timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!acquired) {
          log.warn("Could not acquire lock for key: {}", key);
          throw new LockAcquisitionException("Failed to acquire lock: " + key);
        }
        log.info("Acquired lock for key: {}", key);
        task.run();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new LockAcquisitionException("Interrupted while acquiring lock", e);
      } finally {
        if (acquired && lock.isHeldByCurrentThread()) {
          lock.unlock();
          log.info("Released lock for key: {}", key);
        }
      }
    }, LockAcquisitionException.class);
  }
}
