package org.atlas.product.persistence.jpa.repository;

import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.product.persistence.jpa.entity.JpaOptimisticProduct;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOptimisticProductRepository extends
    JpaBaseRepository<JpaOptimisticProduct, Integer> {

}
