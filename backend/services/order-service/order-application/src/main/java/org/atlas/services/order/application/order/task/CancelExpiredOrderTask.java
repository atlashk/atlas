package org.atlas.services.order.application.order.task;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.contract.order.OrderExpiredEvent;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.port.out.messaging.OrderEventMessagePublisher;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelExpiredOrderTask {

  private static final long EXPIRATION_MINUTES = 15;
  private static final String CANCELLATION_REASON =
      "Order expired after " + EXPIRATION_MINUTES + " minutes without fulfillment";

  private final OrderRepository orderRepository;
  private final OrderEventMessagePublisher orderEventMessagePublisher;

  @Transactional
  public int execute() {
    LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
    List<OrderEntity> expiredOrders = orderRepository.findExpiredOrders(expiredBefore);
    if (expiredOrders.isEmpty()) {
      return 0;
    }

    for (OrderEntity order : expiredOrders) {
      // Update order
      order.setStatus(OrderStatus.CANCELED);
      order.setCancellationReason(CANCELLATION_REASON);
      orderRepository.update(order);

      OrderExpiredEvent event = new OrderExpiredEvent();
      event.setOrderId(order.getId());
      orderEventMessagePublisher.publish(event);
    }

    log.info("Cancelled {} expired orders", expiredOrders.size());
    return expiredOrders.size();
  }
}
