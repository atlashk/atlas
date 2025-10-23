
package org.atlas.framework.util;

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
}
