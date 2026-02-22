package org.atlas.services.catalog.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.atlas.services.catalog.port.out.repository.ProductRepository;

public interface CustomJpaProductRepository {

  List<JpaProductEntity> findByCriteria(ProductRepository.FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(ProductRepository.FindProductCriteria criteria);
}
