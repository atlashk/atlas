package org.atlas.framework.saga.command;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.util.StringUtil;

@Builder
@Getter
@Setter
public class SagaCommandResult implements Serializable {

  private boolean success;
  private Object result;
  private String errorMessage;

  public static SagaCommandResult success(Object result) {
    return SagaCommandResult.builder()
        .success(true)
        .result(result)
        .build();
  }

  public static SagaCommandResult failure(String errorMessage) {
    return SagaCommandResult.builder()
        .success(false)
        .errorMessage(errorMessage)
        .build();
  }
}
