package org.atlas.domain.order.repository;

import java.math.BigDecimal;
import java.util.Optional;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface OrderRepository {

  PagingResult<Order> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  Long countAll();

  BigDecimal sumAmountByStatus(OrderStatus status);

  Optional<Order> findById(Integer id);

  Optional<Order> findBySagaId(Integer sagaId);

  void insert(Order order);

  void update(Order order);
}
