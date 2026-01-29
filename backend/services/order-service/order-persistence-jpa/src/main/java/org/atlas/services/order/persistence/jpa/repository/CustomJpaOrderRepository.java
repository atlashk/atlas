package org.atlas.services.order.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.order.application.port.repository.criteria.FindOrderCriteria;
import org.atlas.services.order.persistence.jpa.entity.JpaOrder;

public interface CustomJpaOrderRepository {

  List<JpaOrder> findByCriteria(FindOrderCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindOrderCriteria criteria);
}
