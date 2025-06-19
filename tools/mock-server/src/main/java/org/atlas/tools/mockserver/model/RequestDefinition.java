package org.atlas.tools.mockserver.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class RequestDefinition {

  private String type;
  private List<ParameterDefinition> parameters;
  private Map<String, Object> schema; // Changed from Map<String, String> to Map<String, Object>
}
