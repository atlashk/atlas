package org.atlas.domain.order.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.aggregator.OrderAggregator;
import org.atlas.domain.order.aggregator.OrderAggregator.AggregationOptions;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.usecase.front.model.ListOrderInput;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingResult;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class ListOrderUseCaseHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;

  public PagingResult<OrderEntity> handle(ListOrderInput input) throws Exception {
    // Find orders
    FindOrderCriteria criteria = ObjectMapperUtil.getInstance()
        .map(input, FindOrderCriteria.class);
    criteria.setUserId(Contexts.getUserId());
    PagingResult<OrderEntity> orderPage = orderRepository.findByCriteria(criteria,
        input.getPagingRequest());
    if (orderPage.checkEmpty()) {
      return PagingResult.empty();
    }

    // Aggregate orders
    orderAggregator.aggregate(
        orderPage.getData(),
        AggregationOptions.builder()
            .loadUsers(true)
            .loadProducts(true)
            .loadPayments(true)
            .build()
    );

    return orderPage;
  }
}
