package org.atlas.framework.sequencegenerator;

import org.atlas.framework.sequencegenerator.enums.SequenceType;

public interface SequenceGenerator {

  String generate(SequenceType sequenceType);
}
