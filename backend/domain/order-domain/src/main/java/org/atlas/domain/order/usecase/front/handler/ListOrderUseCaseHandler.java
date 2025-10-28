package org.atlas.domain.order.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.usecase.front.mapper.OrderMapper;
import org.atlas.domain.order.usecase.front.model.ListOrderInput;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class ListOrderUseCaseHandler {

  private final OrderRepository orderRepository;

  public PagingResult<Order> handle(ListOrderInput input) throws Exception {
    FindOrderCriteria criteria = OrderMapper.INSTANCE.toFindOrderCriteria(input);
    criteria.setUserId(Contexts.getUserId());
    return orderRepository.findByCriteria(criteria, input.getPagingRequest());
  }
}
