package org.atlas.framework.lock;

import java.time.Duration;

public interface LockPort {

  boolean acquireLock(String key, Duration waitTime, Duration leaseTime);

  void releaseLock(String key);
}
