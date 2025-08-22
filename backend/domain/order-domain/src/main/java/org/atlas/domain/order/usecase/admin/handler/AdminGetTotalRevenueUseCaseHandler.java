package org.atlas.domain.order.usecase.admin.handler;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminGetTotalRevenueUseCaseHandler {

  private final OrderRepository orderRepository;

  public BigDecimal handle() throws Exception {
    return orderRepository.sumAmountByStatus(OrderStatus.CONFIRMED);
  }
}
