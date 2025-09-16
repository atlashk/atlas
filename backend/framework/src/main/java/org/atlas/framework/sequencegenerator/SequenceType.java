package org.atlas.framework.sequencegenerator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum SequenceType {

  ORDER("order", "ORD", 4);

  private final String name;
  private final String prefix;
  private final int padding;
}
