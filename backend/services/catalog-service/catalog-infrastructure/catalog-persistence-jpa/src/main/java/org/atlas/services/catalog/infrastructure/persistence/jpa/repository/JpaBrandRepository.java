package org.atlas.services.catalog.infrastructure.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaBrandEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandRepository extends JpaBaseRepository<JpaBrandEntity, Integer> {

}
