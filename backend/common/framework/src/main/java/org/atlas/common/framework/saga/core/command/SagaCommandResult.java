package org.atlas.common.framework.saga.core.command;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCommandResult implements Serializable {

  private boolean success;
  private String error;
  private Object metadata;

  public static SagaCommandResult success() {
    return SagaCommandResult.builder()
        .success(true)
        .build();
  }

  public static SagaCommandResult success(Object metadata) {
    return SagaCommandResult.builder()
        .success(true)
        .metadata(metadata)
        .build();
  }

  public static SagaCommandResult failure(String error, Object... metadata) {
    return SagaCommandResult.builder()
        .success(false)
        .error(error)
        .metadata(metadata)
        .build();
  }
}
