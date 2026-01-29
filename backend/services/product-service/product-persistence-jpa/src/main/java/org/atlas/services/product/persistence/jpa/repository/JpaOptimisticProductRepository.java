package org.atlas.services.product.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.persistence.jpa.entity.JpaOptimisticProduct;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOptimisticProductRepository extends
    JpaBaseRepository<JpaOptimisticProduct, Integer> {

}
