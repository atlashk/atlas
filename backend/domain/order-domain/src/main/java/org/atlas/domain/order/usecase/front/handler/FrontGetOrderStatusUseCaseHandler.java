package org.atlas.domain.order.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.usecase.front.model.FrontGetOrderStatusOutput;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontGetOrderStatusUseCaseHandler {

  private final OrderRepository orderRepository;

  public FrontGetOrderStatusOutput handle(Integer orderId) throws Exception {
    OrderEntity orderEntity = orderRepository.findById(orderId)
        .orElseThrow(() -> new DomainException(DomainError.ORDER_NOT_FOUND));
    return FrontGetOrderStatusOutput.builder()
        .status(orderEntity.getStatus())
        .cancellationReason(orderEntity.getCancellationReason())
        .build();
  }
}
