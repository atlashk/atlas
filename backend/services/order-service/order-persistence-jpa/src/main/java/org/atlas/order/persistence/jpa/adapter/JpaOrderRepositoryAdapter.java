package org.atlas.order.persistence.jpa.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.order.application.admin.model.AdminMonthlyOrderAggregation;
import org.atlas.order.application.port.repository.OrderRepository;
import org.atlas.order.application.port.repository.criteria.FindOrderCriteria;
import org.atlas.order.domain.entity.Order;
import org.atlas.common.framework.domain.order.OrderStatus;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.common.framework.paging.PagingResult;
import org.atlas.common.framework.util.ObjectMapperUtil;
import org.atlas.order.persistence.jpa.entity.JpaOrder;
import org.atlas.order.persistence.jpa.mapper.JpaOrderMapper;
import org.atlas.order.persistence.jpa.repository.CustomJpaOrderRepository;
import org.atlas.order.persistence.jpa.repository.JpaOrderRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaOrderRepositoryAdapter implements OrderRepository {

  private final JpaOrderRepository jpaOrderRepository;
  private final CustomJpaOrderRepository customJpaOrderRepository;

  @Override
  public PagingResult<Order> findByCriteria(FindOrderCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaOrderRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaOrder> jpaOrders = customJpaOrderRepository.findByCriteria(criteria,
        pagingRequest);
    List<Order> orders = ObjectMapperUtil.mapList(jpaOrders, JpaOrderMapper.INSTANCE::toOrder);
    return PagingResult.of(orders, totalCount, pagingRequest);
  }

  @Override
  public Optional<Order> findById(Integer id) {
    return jpaOrderRepository.findByIdAndFetch(id)
        .map(JpaOrderMapper.INSTANCE::toOrder);
  }

  @Override
  public Optional<Order> findBySagaId(Integer sagaId) {
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
  public void insert(Order order) {
    JpaOrder jpaOrder = JpaOrderMapper.INSTANCE.toJpaOrder(order);
    jpaOrderRepository.insert(jpaOrder);
    order.setId(jpaOrder.getId());
    order.setCreatedAt(jpaOrder.getCreatedAt());
  }

  @Override
  public void update(Order order) {
    JpaOrder jpaOrder = JpaOrderMapper.INSTANCE.toJpaOrder(order);
    jpaOrderRepository.save(jpaOrder);
  }
}
