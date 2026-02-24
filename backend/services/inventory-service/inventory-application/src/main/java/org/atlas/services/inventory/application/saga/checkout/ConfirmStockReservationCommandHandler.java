package org.atlas.services.inventory.application.saga.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.json.jackson.JacksonService;
import org.atlas.libs.framework.saga.checkout.CheckoutCommand;
import org.atlas.libs.framework.saga.checkout.CheckoutSagaData;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.services.inventory.domain.entity.ReservationStatus;
import org.atlas.services.inventory.port.out.repository.ReservationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmStockReservationCommandHandler {

  private final ReservationRepository reservationRepository;

  @SagaCommandHandler(command = CheckoutCommand.CONFIRM_STOCK_RESERVATION)
  public SagaCommandResult confirmStockReservation(SagaCommand sagaCommand) {
    SagaContext sagaContext = SagaContext.deserialize(sagaCommand.getSagaContext());
    CheckoutSagaData checkoutSagaData = JacksonService.OBJECT_MAPPER.convertValue(
        sagaContext.get("data"), CheckoutSagaData.class);
    if (checkoutSagaData == null) {
      throw new IllegalArgumentException("Checkout data is required in the saga context");
    }

    // Update all reservations of order to be CONFIRMED
    final String orderId = checkoutSagaData.getOrderId();
    reservationRepository.updateStatus(orderId, ReservationStatus.CONFIRMED);

    log.info("Successfully confirmed stock reservation: sagaId={}, orderId={}",
        sagaCommand.getSagaId(), checkoutSagaData.getOrderId());
    return SagaCommandResult.success();
  }
}
