package org.atlas.libs.saga.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSagaCommandEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaCommandRepository extends JpaBaseRepository<JpaSagaCommandEntity, Integer> {

  List<JpaSagaCommandEntity> findBySagaId(Integer sagaId);

  Optional<JpaSagaCommandEntity> findBySagaIdAndName(Integer sagaId, String name);
}
