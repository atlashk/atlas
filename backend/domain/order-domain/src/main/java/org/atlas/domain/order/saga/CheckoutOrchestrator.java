package org.atlas.domain.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.annotation.SagaOrchestrator;
import org.atlas.framework.saga.annotation.StartSaga;
import org.atlas.framework.saga.command.SagaCommandType;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.lifecycle.SagaLifecycle;

@SagaOrchestrator(
    sagaName = "checkout",
    description = "Orchestrates the checkout process"
)
@RequiredArgsConstructor
@Slf4j
public class CheckoutOrchestrator {

  private final SagaLifecycle sagaLifecycle;

  @StartSaga
  public void startSaga(SagaContext context) {
    sagaLifecycle.sendCommand(context.getSagaId(), SagaCommandType.CREATE_ORDER);
  }

  @SagaCommandReplyHandler(command = SagaCommandType.CREATE_ORDER)
  public void handleCreateOrderReply(SagaContext context) {
    sagaLifecycle.sendCommand(context.getSagaId(), SagaCommandType.RESERVE_PRODUCT);
  }

  @SagaCommandReplyHandler(command = SagaCommandType.RESERVE_PRODUCT)
  public void handleReserveProductReply(SagaContext context) {
    sagaLifecycle.sendCommand(context.getSagaId(), SagaCommandType.INITIALIZE_PAYMENT);
  }

  @SagaCommandReplyHandler(command = SagaCommandType.PROCESS_PAYMENT)
  public void handleProcessPaymentReply(SagaContext context) {
    sagaLifecycle.sendCommand(context.getSagaId(), SagaCommandType.SEND_NOTIFICATION);
    sagaLifecycle.endSaga(context.getSagaId());
  }
}
