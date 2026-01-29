package org.atlas.libs.framework.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.collection.CollectionUtil;

@UtilityClass
public class EnumerationUtil {

  // Optional: cache results per enum class to avoid re-allocations
  private static final ConcurrentHashMap<Class<?>, Set<String>> NAMES_CACHE = new ConcurrentHashMap<>();

  public static <E extends Enum<E>> Set<String> allNames(Class<E> enumClass) {
    if (enumClass == null) {
      throw new IllegalArgumentException("Enum class must not be null");
    }
    if (!enumClass.isEnum()) {
      throw new IllegalArgumentException(enumClass + " is not an enum");
    }

    return NAMES_CACHE.computeIfAbsent(enumClass, cls -> {
      E[] enumConstants = enumClass.getEnumConstants();
      if (ArrayUtil.isEmpty(enumConstants)) {
        return CollectionUtil.emptySet();
      }

      Set<String> names = new LinkedHashSet<>(enumConstants.length);
      Arrays.stream(enumConstants)
          .map(Enum::name)
          .forEach(names::add);

      return Collections.unmodifiableSet(names);
    });
  }
}
