package org.atlas.libs.framework.cache;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ApplicationCache {

  CART("cart", 300),
  PRODUCT("product", 300);

  private final String name;
  private final long ttl; // Time-to-live in seconds

  private static final Map<String, ApplicationCache> BY_NAME =
      Arrays.stream(values())
          .collect(Collectors.toUnmodifiableMap(ApplicationCache::getName, Function.identity()));

  public static Optional<ApplicationCache> findByName(String name) {
    return Optional.ofNullable(BY_NAME.get(name));
  }

  public static ApplicationCache requireByName(String name) {
    ApplicationCache v = BY_NAME.get(name);
    if (v == null) {
      throw new IllegalArgumentException("Unknown cache: " + name);
    }
    return v;
  }
}
