package org.atlas.framework.saga.event;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StepExecutionReply {

  private Long stepId;
  private boolean success;
  private String errorMessage;
  private Map<String, Object> metadata;
}
