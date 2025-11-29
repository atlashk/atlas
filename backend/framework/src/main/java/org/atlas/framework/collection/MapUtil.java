package org.atlas.framework.collection;

import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapUtil {

  public static boolean isEmpty(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  public static boolean isNotEmpty(Map<?, ?> map) {
    return !isEmpty(map);
  }
}
