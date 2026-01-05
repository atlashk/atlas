package org.atlas.common.infrastructure.saga.persistence.jpa.repository;

import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.common.infrastructure.saga.persistence.jpa.entity.JpaSaga;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaRepository extends JpaBaseRepository<JpaSaga, Integer> {

}
