package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.util.StringUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCommandReplyEvent {

  private Long sagaId;
  private String sagaName;
  private String sagaCommandName;
  private boolean success;
  private Object result;
  private String errorMessage;

  @Override
  public String toString() {
    return "{" +
        "sagaId=" + sagaId +
        ", sagaName='" + sagaName + '\'' +
        ", sagaCommandName='" + sagaCommandName + '\'' +
        ", success=" + success +
        '}';
  }

  public static SagaCommandReplyEvent success(SagaEntity sagaEntity, String sagaCommandName,
      Object result) {
    return SagaCommandReplyEvent.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaCommandName(sagaCommandName)
        .success(true)
        .result(result)
        .build();
  }

  public static SagaCommandReplyEvent failure(SagaEntity sagaEntity, String sagaCommandName,
      Throwable throwable) {
    return SagaCommandReplyEvent.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaCommandName(sagaCommandName)
        .success(false)
        .errorMessage(StringUtil.sanitizeErrorMessage(throwable.getMessage()))
        .build();
  }
}
