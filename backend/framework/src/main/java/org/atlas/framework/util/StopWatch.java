package org.atlas.framework.util;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe StopWatch
 */
public class StopWatch {

  private long startTime = 0;
  private long endTime = 0;
  private boolean running = false;
  private final ReentrantLock lock = new ReentrantLock();

  public void start() {
    lock.lock();
    try {
      if (!running) {
        startTime = System.nanoTime();
        running = true;
      }
    } finally {
      lock.unlock();
    }
  }

  public void stop() {
    lock.lock();
    try {
      if (running) {
        endTime = System.nanoTime();
        running = false;
      }
    } finally {
      lock.unlock();
    }
  }

  public void reset() {
    lock.lock();
    try {
      startTime = 0;
      endTime = 0;
      running = false;
    } finally {
      lock.unlock();
    }
  }

  public long getElapsedTimeMs() {
    lock.lock();
    try {
      if (running) {
        return (System.nanoTime() - startTime) / 1_000_000;
      } else {
        return (endTime - startTime) / 1_000_000;
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean isRunning() {
    lock.lock();
    try {
      return running;
    } finally {
      lock.unlock();
    }
  }
}
