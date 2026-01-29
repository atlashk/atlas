package org.atlas.libs.framework.util;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberUtil {

  public static boolean isInteger(String str) {
    if (StringUtil.isBlank(str)) {
      return false;
    }
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean isLong(String str) {
    if (StringUtil.isBlank(str)) {
      return false;
    }
    try {
      Long.parseLong(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean isZero(Integer val) {
    if (val == null) {
      return false;
    }
    return val == 0;
  }

  public static boolean isZero(Long val) {
    if (val == null) {
      return false;
    }
    return val == 0L;
  }

  public static boolean isZero(BigDecimal val) {
    if (val == null) {
      return false;
    }
    return val.compareTo(BigDecimal.ZERO) == 0;
  }

  public static boolean isPositive(BigDecimal val) {
    if (val == null) {
      return false;
    }
    return val.compareTo(BigDecimal.ZERO) > 0;
  }
}
