package org.atlas.infrastructure.persistence.jpa.impl.order.repository;

import java.util.List;
import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.impl.order.entity.JpaOrder;

public interface CustomJpaOrderRepository {

  List<JpaOrder> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindOrderCriteria criteria);
}
