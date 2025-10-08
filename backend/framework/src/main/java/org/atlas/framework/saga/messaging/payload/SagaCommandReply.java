package org.atlas.framework.saga.messaging.payload;

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
public class SagaCommandReply {

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

  public static SagaCommandReply success(SagaEntity sagaEntity, String sagaCommandName,
      Object result) {
    return SagaCommandReply.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaCommandName(sagaCommandName)
        .success(true)
        .result(result)
        .build();
  }

  public static SagaCommandReply failure(SagaEntity sagaEntity, String sagaCommandName,
      Throwable throwable) {
    return SagaCommandReply.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaCommandName(sagaCommandName)
        .success(false)
        .errorMessage(StringUtil.sanitizeErrorMessage(throwable.getMessage()))
        .build();
  }
}
