package org.atlas.services.product.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProduct;
import org.atlas.services.product.port.out.repository.ProductRepository;

public interface CustomJpaProductRepository {

  List<JpaProduct> findByCriteria(ProductRepository.FindProductCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(ProductRepository.FindProductCriteria criteria);
}
