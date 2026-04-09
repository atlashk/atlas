package org.atlas.services.order.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrderEntity;
import org.atlas.services.order.port.out.repository.OrderRepository;

public interface CustomJpaOrderRepository {

  List<JpaOrderEntity> findByCriteria(OrderRepository.FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(OrderRepository.FindOrderCriteria criteria);
}
