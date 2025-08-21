package org.atlas.infrastructure.persistence.jpa.adapter.product.repository;

import java.util.List;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaProductEntity;

public interface CustomJpaProductRepository {

  List<JpaProductEntity> findByCriteria(FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindProductCriteria criteria);
}
