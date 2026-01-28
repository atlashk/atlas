package org.atlas.common.framework.lock;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.kvstore.KvStoreService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(KvStoreService.class)
@RequiredArgsConstructor
@Slf4j
public class LockService {

  private final KvStoreService kvStoreService;

  private static final String STORE_NAME = "locks";
  private static final Duration MIN_BACKOFF = Duration.ofMillis(10);
  private static final Duration MAX_BACKOFF = Duration.ofMillis(200);

  // Track lock ownership to prevent releasing locks we don't own
  private final ConcurrentHashMap<String, String> ownedLocks = new ConcurrentHashMap<>();

  /**
   * Acquires a distributed lock with exponential backoff.
   *
   * @param key       the lock key
   * @param waitTime  maximum time to wait for the lock
   * @param leaseTime how long the lock should be held before auto-expiring
   * @return true if lock was acquired, false otherwise
   */
  public boolean acquireLock(String key, Duration waitTime, Duration leaseTime) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Lock key cannot be null or empty");
    }
    if (waitTime.isNegative() || waitTime.isZero()) {
      throw new IllegalArgumentException("Wait time must be positive");
    }
    if (leaseTime.isNegative() || leaseTime.isZero()) {
      throw new IllegalArgumentException("Lease time must be positive");
    }

    String lockValue = generateLockValue();
    long deadlineNanos = System.nanoTime() + waitTime.toNanos();
    long backoffMillis = MIN_BACKOFF.toMillis();
    int attempts = 0;

    while (System.nanoTime() < deadlineNanos) {
      attempts++;
      try {
        boolean acquired = kvStoreService.putIfAbsent(STORE_NAME, key, lockValue, leaseTime);
        if (acquired) {
          ownedLocks.put(key, lockValue);
          log.debug("Lock acquired for key: {} after {} attempts", key, attempts);
          return true;
        }

        // Exponential backoff with jitter
        long jitter = (long) (Math.random() * backoffMillis * 0.5);
        long sleepTime = backoffMillis + jitter;
        TimeUnit.MILLISECONDS.sleep(Math.min(sleepTime,
            TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime())));

        // Increase backoff for next iteration
        backoffMillis = Math.min(backoffMillis * 2, MAX_BACKOFF.toMillis());

      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        log.warn("Thread interrupted while acquiring lock for key: {}", key);
        return false;
      } catch (Exception e) {
        log.warn("Error while attempting to acquire lock for key: {} (attempt {})", key, attempts,
            e);
        // Continue retrying until deadline
      }
    }

    log.debug("Failed to acquire lock for key: {} after {} attempts", key, attempts);
    return false;
  }

  /**
   * Tries to acquire a lock immediately without waiting.
   *
   * @param key       the lock key
   * @param leaseTime how long the lock should be held before auto-expiring
   * @return true if lock was acquired, false otherwise
   */
  public boolean acquireLock(String key, Duration leaseTime) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Lock key cannot be null or empty");
    }
    if (leaseTime.isNegative() || leaseTime.isZero()) {
      throw new IllegalArgumentException("Lease time must be positive");
    }

    try {
      String lockValue = generateLockValue();
      boolean acquired = kvStoreService.putIfAbsent(STORE_NAME, key, lockValue, leaseTime);
      if (acquired) {
        ownedLocks.put(key, lockValue);
        log.debug("Lock acquired immediately for key: {}", key);
      }
      return acquired;
    } catch (Exception e) {
      log.warn("Error while trying to acquire lock for key: {}", key, e);
      return false;
    }
  }

  /**
   * Releases a lock only if this service instance owns it.
   *
   * @param key the lock key
   * @return true if the lock was released, false if it wasn't owned or couldn't be released
   */
  public boolean releaseLock(String key) {
    String expectedValue = ownedLocks.get(key);
    if (expectedValue == null) {
      log.debug("Cannot release lock for key: {} - not owned by this instance", key);
      return false;
    }

    try {
      // Verify we still own the lock before deleting
      var currentValue = kvStoreService.get(STORE_NAME, key);
      if (currentValue.isPresent() && expectedValue.equals(currentValue.get())) {
        boolean deleted = kvStoreService.delete(STORE_NAME, key);
        if (deleted) {
          ownedLocks.remove(key);
          log.debug("Lock released for key: {}", key);
          return true;
        }
      } else {
        log.warn("Lock for key: {} was already expired or taken by another instance", key);
        ownedLocks.remove(key);
        return false;
      }
    } catch (Exception e) {
      log.warn("Failed to release lock for key: {}", key, e);
    }
    return false;
  }

  /**
   * Checks if a lock is currently held (by anyone).
   *
   * @param key the lock key
   * @return true if the lock exists, false otherwise
   */
  public boolean isLocked(String key) {
    try {
      return kvStoreService.exists(STORE_NAME, key);
    } catch (Exception e) {
      log.warn("Error checking lock status for key: {}", key, e);
      return false;
    }
  }

  /**
   * Checks if this service instance owns the lock.
   *
   * @param key the lock key
   * @return true if this instance owns the lock, false otherwise
   */
  public boolean ownsLock(String key) {
    String expectedValue = ownedLocks.get(key);
    if (expectedValue == null) {
      return false;
    }

    try {
      var currentValue = kvStoreService.get(STORE_NAME, key);
      return currentValue.isPresent() && expectedValue.equals(currentValue.get());
    } catch (Exception e) {
      log.warn("Error checking lock ownership for key: {}", key, e);
      return false;
    }
  }

  /**
   * Generates a unique lock value to identify lock ownership.
   */
  private String generateLockValue() {
    return UUID.randomUUID().toString();
  }
}
