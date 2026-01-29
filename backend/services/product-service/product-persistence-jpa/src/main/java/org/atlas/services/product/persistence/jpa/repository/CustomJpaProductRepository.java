package org.atlas.services.product.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.product.application.port.repository.criteria.FindProductCriteria;
import org.atlas.services.product.persistence.jpa.entity.JpaProduct;

public interface CustomJpaProductRepository {

  List<JpaProduct> findByCriteria(FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindProductCriteria criteria);
}
