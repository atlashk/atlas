package org.atlas.framework.saga.compensation;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.util.StringUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCompensationResult implements Serializable {

  private boolean success;
  private String errorMessage;

  public static SagaCompensationResult success(Object result) {
    return SagaCompensationResult.builder()
        .success(true)
        .build();
  }

  public static SagaCompensationResult failure(Throwable throwable) {
    return SagaCompensationResult.builder()
        .success(false)
        .errorMessage(StringUtil.sanitizeErrorMessage(throwable.getMessage()))
        .build();
  }
}
