package org.atlas.framework.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.event.DomainEvent;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.util.StringUtil;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SagaCommandReplyEvent extends SagaEvent {

  private String sagaCommandName;
  private boolean success;
  private Object result;
  private String errorMessage;

  public SagaCommandReplyEvent(String eventSource) {
    super(eventSource, SagaEventType.SAGA_COMMAND_REPLY.name());
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

  public static SagaCommandReplyEvent success(String eventSource, SagaEntity sagaEntity,
      String sagaCommandName, Object result) {
    SagaCommandReplyEvent instance = new SagaCommandReplyEvent(eventSource);
    instance.sagaId = sagaEntity.getId();
    instance.sagaName = sagaEntity.getName();
    instance.sagaCommandName = sagaCommandName;
    instance.success = true;
    instance.result = result;
    return instance;
  }

  public static SagaCommandReplyEvent failure(String eventSource, SagaEntity sagaEntity,
      String sagaCommandName, Throwable throwable) {
    SagaCommandReplyEvent instance = new SagaCommandReplyEvent(eventSource);
    instance.sagaId = sagaEntity.getId();
    instance.sagaName = sagaEntity.getName();
    instance.sagaCommandName = sagaCommandName;
    instance.success = false;
    instance.errorMessage = StringUtil.sanitizeErrorMessage(throwable.getMessage());
    return instance;
  }
}
