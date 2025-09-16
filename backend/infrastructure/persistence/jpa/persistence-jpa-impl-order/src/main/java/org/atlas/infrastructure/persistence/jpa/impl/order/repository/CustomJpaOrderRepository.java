package org.atlas.infrastructure.persistence.jpa.impl.order.repository;

import java.util.List;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrderEntity;

public interface CustomJpaOrderRepository {

  List<JpaOrderEntity> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindOrderCriteria criteria);
}
