package org.atlas.framework.cache;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Caches {

  CART("cart", 300);

  private final String name;
  private final long ttl;
}
