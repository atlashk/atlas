package org.atlas.domain.order.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.service.OrderAggregator;
import org.atlas.domain.order.usecase.front.model.FrontListOrderInput;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontListOrderUseCaseHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;

  public PagingResult<OrderEntity> handle(FrontListOrderInput input) throws Exception {
    // Query order
    Integer userId = Contexts.getUserId();
    PagingResult<OrderEntity> orderEntityPage = orderRepository.findByUserId(userId,
        input.getPagingRequest());
    if (orderEntityPage.checkEmpty()) {
      return PagingResult.empty();
    }

    // Load users and products
    orderAggregator.aggregate(orderEntityPage.getData(), true);

    return orderEntityPage;
  }
}
