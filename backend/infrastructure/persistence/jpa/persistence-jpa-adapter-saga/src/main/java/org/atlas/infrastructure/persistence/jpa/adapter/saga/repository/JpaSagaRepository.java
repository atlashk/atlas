package org.atlas.infrastructure.persistence.jpa.adapter.saga.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.adapter.saga.entity.JpaSaga;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaRepository extends JpaBaseRepository<JpaSaga, Integer> {

}
