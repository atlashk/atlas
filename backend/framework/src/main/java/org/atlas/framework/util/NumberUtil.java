package org.atlas.framework.util;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberUtil {

  public static boolean isZero(Integer input) {
    if (input == null) {
      return false;
    }
    return input == 0;
  }

  public static boolean isZero(Long input) {
    if (input == null) {
      return false;
    }
    return input == 0L;
  }

  public static boolean isZero(BigDecimal input) {
    if (input == null) {
      return false;
    }
    return input.compareTo(BigDecimal.ZERO) == 0;
  }
}
