
package org.atlas.libs.framework.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SleepUtil {

  public static void sleep(long milliSeconds) {
    try {
      TimeUnit.MILLISECONDS.sleep(milliSeconds);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  public static void sleepRandom(int minSeconds, int maxSeconds) {
    // Generate a random sleep duration between minSeconds and maxSeconds seconds
    int randomSleepDuration = ThreadLocalRandom.current().nextInt(minSeconds, maxSeconds + 1);
    try {
      TimeUnit.SECONDS.sleep(randomSleepDuration);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  public static void sleepJitter(long baseBackOfMs, int attempt) {
    try {
      long delay = (long) (baseBackOfMs * Math.pow(2, attempt - 1));
      long jitter = ThreadLocalRandom.current().nextLong(0, (long) (delay * 0.2));
      Thread.sleep(delay + jitter);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }
}
