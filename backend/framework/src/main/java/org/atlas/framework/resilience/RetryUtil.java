package org.atlas.framework.resilience;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.util.ConcurrentUtil;

@UtilityClass
@Slf4j
public class RetryUtil {

  public static <T extends Throwable> void retryOn(Runnable task,
      Class<T> retryOnException) {
    retryOn(task, retryOnException, 3, 1000, 2);
  }

  public static <T extends Throwable> void retryOn(
      Runnable task,
      Class<T> retryOnException,
      int maxAttempts,
      long initialDelayMs,
      double backoffMultiplier
  ) {
    int attempts = 0;
    long delay = initialDelayMs;

    while (true) {
      try {
        task.run();
        return;
      } catch (Exception ex) {
        attempts++;
        if (!retryOnException.isInstance(ex) || attempts >= maxAttempts) {
          throw ex;
        }

        log.warn("Retrying (attempt {}/{}), cause: {}. Retrying in {} ms",
            attempts, maxAttempts, ex.getMessage(), delay);

        ConcurrentUtil.sleep(delay);

        delay = (long) (delay * backoffMultiplier); // apply backoff
      }
    }
  }
}
