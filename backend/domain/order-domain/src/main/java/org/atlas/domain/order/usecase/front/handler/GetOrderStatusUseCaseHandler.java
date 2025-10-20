package org.atlas.domain.order.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.usecase.front.model.GetOrderStatusInput;
import org.atlas.domain.order.usecase.front.model.GetOrderStatusOutput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class GetOrderStatusUseCaseHandler {

  private final OrderRepository orderRepository;

  public GetOrderStatusOutput handle(GetOrderStatusInput input) throws Exception {
    OrderEntity order = orderRepository.findById(input.getOrderId())
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    return new GetOrderStatusOutput(order.getStatus());
  }
}
