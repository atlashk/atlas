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
public class SagaCompensationReply {

  private Long sagaId;
  private String sagaName;
  private String sagaCommandName;
  private boolean success;
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

  public static SagaCompensationReply success(SagaEntity sagaEntity, String sagaCommandName) {
    return SagaCompensationReply.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaCommandName(sagaCommandName)
        .success(true)
        .build();
  }

  public static SagaCompensationReply failure(SagaEntity sagaEntity, String sagaCommandName,
      Throwable throwable) {
    return SagaCompensationReply.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaCommandName(sagaCommandName)
        .success(false)
        .errorMessage(StringUtil.sanitizeErrorMessage(throwable.getMessage()))
        .build();
  }
}
