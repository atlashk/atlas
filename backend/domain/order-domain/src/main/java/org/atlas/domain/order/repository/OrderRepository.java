package org.atlas.domain.order.repository;

import java.math.BigDecimal;
import java.util.Optional;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface OrderRepository {

  PagingResult<OrderEntity> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  Long countAll();

  BigDecimal sumAmountByStatus(OrderStatus status);

  Optional<OrderEntity> findById(Integer id);

  void insert(OrderEntity order);

  void update(OrderEntity order);
}
