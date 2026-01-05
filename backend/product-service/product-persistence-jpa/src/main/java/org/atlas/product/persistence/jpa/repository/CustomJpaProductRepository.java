package org.atlas.product.persistence.jpa.repository;

import java.util.List;
import org.atlas.product.application.port.repository.criteria.FindProductCriteria;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.product.persistence.jpa.entity.JpaProduct;

public interface CustomJpaProductRepository {

  List<JpaProduct> findByCriteria(FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindProductCriteria criteria);
}
