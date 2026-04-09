package org.atlas.libs.framework.sequencegenerator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SequenceType {

  // Identity entities
  USER("user", "USR", 7),

  // Catalog entities
  PRODUCT("product", "PRD", 7),

  // Inventory entities
  RESERVATION("reservation", "RSV", 7),

  // Order entities
  ORDER("order", "ORD", 7),

  // Payment entities
  PAYMENT("payment", "PAY", 7);

  private final String name;
  private final String prefix;
  private final int padding;
}
