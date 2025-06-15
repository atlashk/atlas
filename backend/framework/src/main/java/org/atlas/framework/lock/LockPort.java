package org.atlas.framework.lock;

import java.time.Duration;

public interface LockPort {

  void doWithLock(Runnable task, String key, Duration timeout) throws LockAcquisitionException;
}
