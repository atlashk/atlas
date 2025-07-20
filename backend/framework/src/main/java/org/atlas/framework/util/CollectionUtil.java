package org.atlas.framework.util;

import java.util.Collection;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionUtil {

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isNotEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }
}
