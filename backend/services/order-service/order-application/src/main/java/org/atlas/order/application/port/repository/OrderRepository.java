package org.atlas.order.application.port.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.atlas.order.application.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.order.application.port.repository.criteria.FindOrderCriteria;
import org.atlas.order.domain.entity.Order;
import org.atlas.common.framework.domain.order.OrderStatus;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.common.framework.paging.PagingResult;

public interface OrderRepository {

  PagingResult<Order> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  Long countAll();

  BigDecimal sumAmountByStatus(OrderStatus status);

  List<AdminMonthlyOrderAggregation> aggregateMonthlyByStatus(OrderStatus status);

  Optional<Order> findById(Integer id);

  Optional<Order> findBySagaId(Integer sagaId);

  void insert(Order order);

  void update(Order order);
}
