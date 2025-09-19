package org.atlas.framework.lock;

import java.time.Duration;

public interface LockPort {

  /**
   * Executes a task while holding a distributed lock with the specified key.
   *
   * @param task               the task to execute while holding the lock
   * @param key                the unique identifier for the lock
   * @param waitTime           the maximum time to wait for acquiring the lock
   * @param leaseTime          the maximum time to hold the lock before automatic release
   * @param unlockOnCompletion whether to release the lock after task completion
   * @throws LockAcquisitionException if the lock cannot be acquired within the wait time
   */
  void doWithLock(Runnable task, String key, Duration waitTime, Duration leaseTime,
      boolean unlockOnCompletion) throws LockAcquisitionException;
}
