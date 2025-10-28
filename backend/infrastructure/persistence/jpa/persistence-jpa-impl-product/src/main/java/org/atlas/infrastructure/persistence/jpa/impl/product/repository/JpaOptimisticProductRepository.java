package org.atlas.infrastructure.persistence.jpa.impl.product.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaOptimisticProduct;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOptimisticProductRepository extends
    JpaBaseRepository<JpaOptimisticProduct, Integer> {

}
