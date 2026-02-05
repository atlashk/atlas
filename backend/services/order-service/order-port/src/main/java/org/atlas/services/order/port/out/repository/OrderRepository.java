package org.atlas.services.order.port.out.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.port.in.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.services.order.port.out.repository.criteria.FindOrderCriteria;

public interface OrderRepository {

  PagingResult<OrderEntity> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  Long countAll();

  BigDecimal sumAmountByStatus(OrderStatus status);

  List<AdminMonthlyOrderAggregation> aggregateMonthlyByStatus(OrderStatus status);

  Optional<OrderEntity> findByOrderId(String orderId);

  Optional<OrderEntity> findBySagaId(Integer sagaId);

  void insert(OrderEntity order);

  void update(OrderEntity order);

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  class FindOrderCriteria {

    private String orderId;

    private String userId;

    private String productId;

    private OrderStatus status;

    private Date startDate;

    private Date endDate;
  }
}
