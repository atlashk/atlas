package org.atlas.infrastructure.persistence.jpa.impl.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.impl.order.mapper.JpaOrderEntityMapper;
import org.atlas.infrastructure.persistence.jpa.impl.order.repository.CustomJpaOrderRepository;
import org.atlas.infrastructure.persistence.jpa.impl.order.repository.JpaOrderRepository;
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
    List<JpaOrderEntity> jpaOrderEntities = customJpaOrderRepository.findByCriteria(criteria,
        pagingRequest);
    List<OrderEntity> orders = ObjectMapperUtil.getInstance()
        .mapList(jpaOrderEntities, JpaOrderEntityMapper::toOrderEntity);
    return PagingResult.of(orders, totalCount, pagingRequest);
  }

  @Override
  public Optional<OrderEntity> findById(Integer id) {
    return jpaOrderRepository.findByIdAndFetch(id)
        .map(JpaOrderEntityMapper::toOrderEntity);
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
  public void insert(OrderEntity order) {
    JpaOrderEntity jpaOrderEntity = JpaOrderEntityMapper.toJpaOrderEntity(order);
    jpaOrderRepository.insert(jpaOrderEntity);
    order.setId(jpaOrderEntity.getId());
  }

  @Override
  public void update(OrderEntity order) {
    JpaOrderEntity jpaOrderEntity = JpaOrderEntityMapper.toJpaOrderEntity(order);
    jpaOrderRepository.save(jpaOrderEntity);
  }
}
