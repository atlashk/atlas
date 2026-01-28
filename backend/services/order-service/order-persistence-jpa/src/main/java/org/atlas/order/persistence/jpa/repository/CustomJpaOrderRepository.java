package org.atlas.order.persistence.jpa.repository;

import java.util.List;
import org.atlas.order.application.port.repository.criteria.FindOrderCriteria;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.order.persistence.jpa.entity.JpaOrder;

public interface CustomJpaOrderRepository {

  List<JpaOrder> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindOrderCriteria criteria);
}
