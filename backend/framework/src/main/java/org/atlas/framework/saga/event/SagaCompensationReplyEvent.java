package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.util.StringUtil;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SagaCompensationReplyEvent extends SagaEvent {

  private String sagaCommandName;
  private boolean success;
  private String errorMessage;

  public SagaCompensationReplyEvent(String eventSource) {
    super(eventSource, SagaEventType.SAGA_COMPENSATION_REPLY.name());
  }

  @Override
  public String toString() {
    return "{" +
        "sagaId=" + sagaId +
        ", sagaName='" + sagaName + '\'' +
        ", sagaCommandName='" + sagaCommandName + '\'' +
        ", success=" + success +
        '}';
  }

  public static SagaCompensationReplyEvent success(String eventSource, SagaEntity sagaEntity,
      String sagaCommandName) {
    SagaCompensationReplyEvent instance = new SagaCompensationReplyEvent(eventSource);
    instance.sagaId = sagaEntity.getId();
    instance.sagaName = sagaEntity.getName();
    instance.sagaCommandName = sagaCommandName;
    instance.success = true;
    return instance;
  }

  public static SagaCompensationReplyEvent failure(String eventSource, SagaEntity sagaEntity,
      String sagaCommandName, Throwable throwable) {
    SagaCompensationReplyEvent instance = new SagaCompensationReplyEvent(eventSource);
    instance.sagaId = sagaEntity.getId();
    instance.sagaName = sagaEntity.getName();
    instance.sagaCommandName = sagaCommandName;
    instance.success = false;
    instance.errorMessage = StringUtil.sanitizeErrorMessage(throwable.getMessage());
    return instance;
  }
}
