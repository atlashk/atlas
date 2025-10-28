package org.atlas.infrastructure.persistence.jpa.impl.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaCommand;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaCommandRepository extends JpaBaseRepository<JpaSagaCommand, Integer> {

  List<JpaSagaCommand> findBySagaId(Integer sagaId);

  Optional<JpaSagaCommand> findBySagaIdAndName(Integer sagaId, String name);
}
