package org.atlas.services.inventory.infrastructure.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaOptimisticStockEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOptimisticStockRepository extends
    JpaBaseRepository<JpaOptimisticStockEntity, String> {

}
