package org.atlas.services.order.infrastructure.persistence.jpa.adapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.order.domain.entity.OrderEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrderEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.mapper.JpaOrderMapper;
import org.atlas.services.order.infrastructure.persistence.jpa.repository.CustomJpaOrderRepository;
import org.atlas.services.order.infrastructure.persistence.jpa.repository.JpaOrderRepository;
import org.atlas.services.order.port.in.order.model.admin.MonthlyOrderAggregation;
import org.atlas.services.order.port.out.repository.OrderRepository;
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
    List<JpaOrderEntity> jpaOrders = customJpaOrderRepository.findByCriteria(criteria,
        pagingRequest);
    List<OrderEntity> orders = MapperUtil.mapList(jpaOrders, JpaOrderMapper.INSTANCE::toOrder);
    return PagingResult.of(orders, totalCount, pagingRequest);
  }

  @Override
  public Optional<OrderEntity> findById(String id) {
    return jpaOrderRepository.findByIdAndFetch(id)
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
  public List<MonthlyOrderAggregation> aggregateMonthlyByStatus(OrderStatus status) {
    return jpaOrderRepository.aggregateMonthlyByStatus(status);
  }

  @Override
  public void insert(OrderEntity order) {
    JpaOrderEntity jpaOrder = JpaOrderMapper.INSTANCE.toJpaOrder(order);
    jpaOrderRepository.insert(jpaOrder);
    order.setCreatedAt(jpaOrder.getCreatedAt());
  }

  @Override
  public void update(OrderEntity order) {
    JpaOrderEntity jpaOrder = JpaOrderMapper.INSTANCE.toJpaOrder(order);
    jpaOrderRepository.save(jpaOrder);
  }

  @Override
  public List<OrderEntity> findExpiredOrders(LocalDateTime createdBefore) {
    List<JpaOrderEntity> jpaOrders = jpaOrderRepository.findExpiredOrders(createdBefore);
    return MapperUtil.mapList(jpaOrders, JpaOrderMapper.INSTANCE::toOrder);
  }
}
