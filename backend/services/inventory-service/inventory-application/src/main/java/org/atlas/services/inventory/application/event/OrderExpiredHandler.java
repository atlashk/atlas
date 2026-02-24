package org.atlas.services.inventory.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.DomainEventType;
import org.atlas.libs.framework.domain.event.contract.order.OrderExpiredEvent;
import org.atlas.libs.framework.domain.event.handler.DomainEventHandler;
import org.atlas.services.inventory.domain.entity.ReservationStatus;
import org.atlas.services.inventory.port.out.repository.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;

@DomainEventHandler(type = DomainEventType.ORDER_EXPIRED)
@RequiredArgsConstructor
@Slf4j
public class OrderExpiredHandler {
  
  private final ReservationRepository reservationRepository;
  
  @Transactional
  public void handle(OrderExpiredEvent event) {
    // Update all reservations of order to be EXPIRED
    final String orderId = event.getOrderId();
    reservationRepository.updateStatus(orderId, ReservationStatus.EXPIRED);
    log.info("Updated reservation status to EXPIRED for orderId: {}", orderId);
  }
}
