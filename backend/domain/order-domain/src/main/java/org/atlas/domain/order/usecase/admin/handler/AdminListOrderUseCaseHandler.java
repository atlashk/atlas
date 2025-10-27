package org.atlas.domain.order.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.mapper.OrderMapper;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.usecase.admin.model.AdminListOrderInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminListOrderUseCaseHandler {

  private final OrderRepository orderRepository;

  public PagingResult<OrderEntity> handle(AdminListOrderInput input) throws Exception {
    // Find orders
    FindOrderCriteria criteria = OrderMapper.INSTANCE.toFindOrderCriteria(input);
    return orderRepository.findByCriteria(criteria, input.getPagingRequest());
  }
}
