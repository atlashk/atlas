package org.atlas.infrastructure.persistence.jpa.impl.saga.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaRepository extends JpaBaseRepository<JpaSagaEntity, Integer> {

}
