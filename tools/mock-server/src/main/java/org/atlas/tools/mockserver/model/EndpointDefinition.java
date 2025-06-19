package org.atlas.tools.mockserver.model;

import lombok.Data;

@Data
public class EndpointDefinition {

  private String path;
  private String method;
  private RequestDefinition request;
  private ResponseDefinition response;
}
