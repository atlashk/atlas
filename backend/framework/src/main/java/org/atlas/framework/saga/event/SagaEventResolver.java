package org.atlas.framework.saga.event;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.saga.command.SagaCommandHandlerDispatcher;
import org.atlas.framework.saga.compensation.SagaCompensationHandlerDispatcher;
import org.atlas.framework.saga.orchestrator.SagaOrchestrator;
import org.atlas.framework.saga.orchestrator.SagaRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventResolver {

  private final SagaRegistry sagaRegistry;
  private final SagaOrchestrator sagaOrchestrator;
  private final SagaCommandHandlerDispatcher sagaCommandHandlerDispatcher;
  private final SagaCompensationHandlerDispatcher sagaCompensationHandlerDispatcher;

  public void resolve(SagaEvent sagaEvent) {
    if (SagaEventType.SAGA_COMMAND.name().equalsIgnoreCase(sagaEvent.getEventType())) {
      sagaCommandHandlerDispatcher.dispatch((SagaCommandEvent) sagaEvent);
    } else if (SagaEventType.SAGA_COMPENSATION.name().equalsIgnoreCase(sagaEvent.getEventType())) {
      sagaCompensationHandlerDispatcher.dispatch((SagaCompensationEvent) sagaEvent);
    } else {
      if (sagaRegistry.hasSagaMetadata(sagaEvent.getSagaName())) {
        if (SagaEventType.SAGA_COMMAND_REPLY.name().equalsIgnoreCase(sagaEvent.getEventType())) {
          sagaOrchestrator.handleSagaCommandReplyEvent((SagaCommandReplyEvent) sagaEvent);
          return;
        } else if (SagaEventType.SAGA_COMPENSATION_REPLY.name()
            .equalsIgnoreCase(sagaEvent.getEventType())) {
          sagaOrchestrator.handleSagaCompensationReplyEvent((SagaCompensationReplyEvent) sagaEvent);
          return;
        }
      }
      throw new IllegalArgumentException("Unsupported event type: " + sagaEvent.getEventType());
    }
  }
}
