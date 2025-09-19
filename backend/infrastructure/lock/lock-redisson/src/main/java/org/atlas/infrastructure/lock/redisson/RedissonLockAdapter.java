package org.atlas.infrastructure.lock.redisson;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.lock.LockAcquisitionException;
import org.atlas.framework.lock.LockPort;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedissonLockAdapter implements LockPort {

  private final RedissonClient redissonClient;

  @Override
  public void doWithLock(Runnable task, String key, Duration waitTime, Duration leaseTime, boolean unlockOnCompletion)
      throws LockAcquisitionException {
    try {
      // Try acquiring the lock
      RLock lock = redissonClient.getLock(key);
      boolean acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(),
          TimeUnit.MILLISECONDS);
      if (!acquired) {
        throw new LockAcquisitionException("Failed to acquire lock for key " + key);
      }
      log.info("Acquired lock for key {}", key);

      // Execute the task within the lock
      task.run();

      // Release the lock if specified
      if (unlockOnCompletion && lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.info("Released lock for key {}", key);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new LockAcquisitionException("Interrupted while acquiring lock for key " + key, e);
    }
  }
}
