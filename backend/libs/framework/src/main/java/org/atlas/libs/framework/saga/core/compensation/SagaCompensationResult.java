package org.atlas.libs.framework.saga.core.compensation;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.error.ErrorUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCompensationResult implements Serializable {

  private boolean success;
  private String error;

  public static SagaCompensationResult success() {
    return SagaCompensationResult.builder()
        .success(true)
        .build();
  }

  public static SagaCompensationResult failure(Throwable throwable) {
    return SagaCompensationResult.builder()
        .success(false)
        .error(ErrorUtil.sanitizeErrorMessage(throwable.getMessage()))
        .build();
  }
}
