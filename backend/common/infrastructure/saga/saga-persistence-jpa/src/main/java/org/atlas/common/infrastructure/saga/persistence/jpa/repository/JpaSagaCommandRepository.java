package org.atlas.common.infrastructure.saga.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.common.infrastructure.saga.persistence.jpa.entity.JpaSagaCommand;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaCommandRepository extends JpaBaseRepository<JpaSagaCommand, Integer> {

  List<JpaSagaCommand> findBySagaId(Integer sagaId);

  Optional<JpaSagaCommand> findBySagaIdAndName(Integer sagaId, String name);
}
