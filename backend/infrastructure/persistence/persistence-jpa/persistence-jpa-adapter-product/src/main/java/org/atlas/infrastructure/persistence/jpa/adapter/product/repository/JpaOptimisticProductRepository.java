package org.atlas.infrastructure.persistence.jpa.adapter.product.repository;

import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaOptimisticProductEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOptimisticProductRepository extends
    JpaBaseRepository<JpaOptimisticProductEntity, Integer> {

}
