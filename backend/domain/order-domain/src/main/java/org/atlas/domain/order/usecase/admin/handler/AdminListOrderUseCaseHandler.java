package org.atlas.domain.order.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.service.OrderAggregator;
import org.atlas.domain.order.usecase.admin.model.AdminListOrderInput;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminListOrderUseCaseHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;

  public PagingResult<OrderEntity> handle(AdminListOrderInput input) throws Exception {
    // Query order
    PagingResult<OrderEntity> orderEntityPage = orderRepository.findAll(input.getPagingRequest());
    if (orderEntityPage.checkEmpty()) {
      return PagingResult.empty();
    }

    // Load users and products
    orderAggregator.aggregate(orderEntityPage.getData(), true);

    return orderEntityPage;
  }
}
