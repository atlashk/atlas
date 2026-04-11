package org.atlas.services.order.port.out.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.domain.entity.Order;
import org.atlas.services.order.port.in.order.model.admin.MonthlyOrderAggregation;

public interface OrderRepository {

  PagingResult<Order> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  Long countAll();

  BigDecimal sumAmountByStatus(OrderStatus status);

  List<MonthlyOrderAggregation> aggregateMonthlyByStatus(OrderStatus status);

  Optional<Order> findById(String id);

  Optional<Order> findBySagaId(Integer sagaId);

  void insert(Order order);

  void update(Order order);

  List<Order> findExpiredOrders(LocalDateTime createdBefore);

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  class FindOrderCriteria {

    private String id;

    private String userId;

    private String productId;

    private OrderStatus status;

    private LocalDate startDate;

    private LocalDate endDate;
  }
}
