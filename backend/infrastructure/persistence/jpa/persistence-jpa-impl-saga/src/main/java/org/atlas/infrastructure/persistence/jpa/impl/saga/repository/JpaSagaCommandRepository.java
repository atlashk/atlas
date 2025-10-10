package org.atlas.infrastructure.persistence.jpa.impl.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaCommandEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaCommandRepository extends JpaBaseRepository<JpaSagaCommandEntity, Integer> {

  List<JpaSagaCommandEntity> findBySagaId(Integer sagaId);

  Optional<JpaSagaCommandEntity> findBySagaIdAndName(Integer sagaId, String name);
}
