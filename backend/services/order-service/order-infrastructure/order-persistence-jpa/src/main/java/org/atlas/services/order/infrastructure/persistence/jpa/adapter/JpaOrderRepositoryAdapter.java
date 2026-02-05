package org.atlas.services.order.infrastructure.persistence.jpa.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.port.in.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.services.order.port.out.repository.OrderRepository;
import org.atlas.services.order.port.out.repository.criteria.FindOrderCriteria;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrder;
import org.atlas.services.order.infrastructure.persistence.jpa.mapper.JpaOrderMapper;
import org.atlas.services.order.infrastructure.persistence.jpa.repository.CustomJpaOrderRepository;
import org.atlas.services.order.infrastructure.persistence.jpa.repository.JpaOrderRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaOrderRepositoryAdapter implements OrderRepository {

  private final JpaOrderRepository jpaOrderRepository;
  private final CustomJpaOrderRepository customJpaOrderRepository;

  @Override
  public PagingResult<OrderEntity> findByCriteria(FindOrderCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaOrderRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaOrder> jpaOrders = customJpaOrderRepository.findByCriteria(criteria,
        pagingRequest);
    List<OrderEntity> orders = ObjectMapperUtil.mapList(jpaOrders, JpaOrderMapper.INSTANCE::toOrder);
    return PagingResult.of(orders, totalCount, pagingRequest);
  }

  @Override
  public Optional<OrderEntity> findByOrderId(String orderId) {
    return jpaOrderRepository.findByOrderIdAndFetch(orderId)
        .map(JpaOrderMapper.INSTANCE::toOrder);
  }

  @Override
  public Optional<OrderEntity> findBySagaId(Integer sagaId) {
    return jpaOrderRepository.findBySagaIdAndFetch(sagaId)
        .map(JpaOrderMapper.INSTANCE::toOrder);
  }

  @Override
  public Long countAll() {
    return jpaOrderRepository.count();
  }

  @Override
  public BigDecimal sumAmountByStatus(OrderStatus status) {
    return jpaOrderRepository.sumAmountByStatus(status);
  }

  @Override
  public List<AdminMonthlyOrderAggregation> aggregateMonthlyByStatus(OrderStatus status) {
    return jpaOrderRepository.aggregateMonthlyByStatus(status);
  }

  @Override
  public void insert(OrderEntity order) {
    JpaOrder jpaOrder = JpaOrderMapper.INSTANCE.toJpaOrder(order);
    jpaOrderRepository.insert(jpaOrder);
    order.setOrderId(jpaOrder.getOrderId());
    order.setCreatedAt(jpaOrder.getCreatedAt());
  }

  @Override
  public void update(OrderEntity order) {
    JpaOrder jpaOrder = JpaOrderMapper.INSTANCE.toJpaOrder(order);
    jpaOrderRepository.save(jpaOrder);
  }
}
