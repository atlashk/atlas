package org.atlas.libs.framework.sequencegenerator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SequenceType {

  USER("user", "USR", 7),
  PRODUCT("product", "PRD", 7),
  ORDER("order", "ORD", 7),
  PAYMENT("payment", "PAY", 7);

  private final String name;
  private final String prefix;
  private final int padding;
}
