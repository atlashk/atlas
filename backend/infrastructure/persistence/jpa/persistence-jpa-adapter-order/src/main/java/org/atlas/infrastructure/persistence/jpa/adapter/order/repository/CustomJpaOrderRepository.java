package org.atlas.infrastructure.persistence.jpa.adapter.order.repository;

import java.util.List;
import org.atlas.application.order.port.repository.criteria.FindOrderCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.adapter.order.entity.JpaOrder;

public interface CustomJpaOrderRepository {

  List<JpaOrder> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindOrderCriteria criteria);
}
