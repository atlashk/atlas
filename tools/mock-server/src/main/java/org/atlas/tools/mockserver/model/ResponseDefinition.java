package org.atlas.tools.mockserver.model;

import lombok.Data;

@Data
public class ResponseDefinition {

  private int status;
  private String body;
}
