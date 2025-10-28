package org.atlas.infrastructure.persistence.jpa.impl.product.repository;

import java.util.List;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProduct;

public interface CustomJpaProductRepository {

  List<JpaProduct> findByCriteria(FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindProductCriteria criteria);
}
