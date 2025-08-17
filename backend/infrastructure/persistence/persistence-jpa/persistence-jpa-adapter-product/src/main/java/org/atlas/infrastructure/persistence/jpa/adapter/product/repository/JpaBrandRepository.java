package org.atlas.infrastructure.persistence.jpa.adapter.product.repository;

import org.atlas.infrastructure.persistence.jpa.adapter.product.entity.JpaBrandEntity;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandRepository extends JpaBaseRepository<JpaBrandEntity, Integer> {

}