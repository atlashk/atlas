package org.atlas.framework.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionUtil {

  public <T> List<T> emptyList() {
    return Collections.emptyList();
  }

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isNotEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }
}
