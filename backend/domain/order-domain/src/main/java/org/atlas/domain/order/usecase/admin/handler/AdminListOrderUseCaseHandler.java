package org.atlas.domain.order.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.aggregator.OrderAggregator;
import org.atlas.domain.order.aggregator.OrderAggregator.AggregationOptions;
import org.atlas.domain.order.usecase.admin.model.AdminListOrderInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingResult;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminListOrderUseCaseHandler {

  private final OrderRepository orderRepository;
  private final OrderAggregator orderAggregator;

  public PagingResult<OrderEntity> handle(AdminListOrderInput input) throws Exception {
    // Find orders
    FindOrderCriteria criteria = ObjectMapperUtil.getInstance()
        .map(input, FindOrderCriteria.class);
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
