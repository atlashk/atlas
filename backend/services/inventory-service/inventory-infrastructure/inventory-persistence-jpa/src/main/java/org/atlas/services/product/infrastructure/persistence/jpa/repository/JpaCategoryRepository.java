package org.atlas.services.product.infrastructure.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaCategoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCategoryRepository extends JpaBaseRepository<JpaCategoryEntity, Integer> {

}
