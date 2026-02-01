package org.atlas.services.order.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.order.port.out.repository.criteria.FindOrderCriteria;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaOrder;

public interface CustomJpaOrderRepository {

  List<JpaOrder> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindOrderCriteria criteria);
}
