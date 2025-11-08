package org.atlas.infrastructure.persistence.jpa.impl.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.Order;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrder;
import org.atlas.infrastructure.persistence.jpa.impl.order.mapper.JpaOrderMapper;
import org.atlas.infrastructure.persistence.jpa.impl.order.repository.CustomJpaOrderRepository;
import org.atlas.infrastructure.persistence.jpa.impl.order.repository.JpaOrderRepository;
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
