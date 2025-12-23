package org.atlas.infrastructure.persistence.jpa.adapter.product.repository;

import java.util.List;
import org.atlas.application.product.port.repository.criteria.FindProductCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaProduct;

public interface CustomJpaProductRepository {

  List<JpaProduct> findByCriteria(FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindProductCriteria criteria);
}
