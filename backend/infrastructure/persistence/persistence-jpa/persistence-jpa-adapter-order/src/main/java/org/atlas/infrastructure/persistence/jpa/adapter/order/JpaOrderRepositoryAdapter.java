package org.atlas.infrastructure.persistence.jpa.adapter.order;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.persistence.jpa.adapter.order.entity.JpaOrderEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.order.mapper.JpaOrderEntityMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.order.repository.CustomJpaOrderRepository;
import org.atlas.infrastructure.persistence.jpa.adapter.order.repository.JpaOrderRepository;
import org.atlas.infrastructure.persistence.jpa.core.paging.PagingConverter;
import org.springframework.data.domain.Pageable;
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
    List<OrderEntity> orderEntities = ObjectMapperUtil.getInstance()
        .mapList(jpaOrderEntities, JpaOrderEntityMapper::toOrderEntity);
    return PagingResult.of(orderEntities, totalCount, pagingRequest);
  }

  @Override
  public Optional<OrderEntity> findById(Integer id) {
    return jpaOrderRepository.findByIdAndFetch(id)
        .map(JpaOrderEntityMapper::toOrderEntity);
  }

  @Override
  public void insert(OrderEntity orderEntity) {
    JpaOrderEntity jpaOrderEntity = JpaOrderEntityMapper.toJpaOrderEntity(orderEntity);
    jpaOrderRepository.insert(jpaOrderEntity);
    orderEntity.setId(jpaOrderEntity.getId());
  }

  @Override
  public void update(OrderEntity orderEntity) {
    JpaOrderEntity jpaOrderEntity = JpaOrderEntityMapper.toJpaOrderEntity(orderEntity);
    jpaOrderRepository.save(jpaOrderEntity);
  }
}
