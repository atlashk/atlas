package org.atlas.libs.saga.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSagaEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaRepository extends JpaBaseRepository<JpaSagaEntity, Integer> {

}
