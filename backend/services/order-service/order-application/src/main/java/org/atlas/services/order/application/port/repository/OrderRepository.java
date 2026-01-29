package org.atlas.services.order.application.port.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.application.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.services.order.application.port.repository.criteria.FindOrderCriteria;
import org.atlas.services.order.domain.entity.Order;

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
