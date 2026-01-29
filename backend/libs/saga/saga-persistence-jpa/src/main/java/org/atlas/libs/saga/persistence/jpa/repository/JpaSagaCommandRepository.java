package org.atlas.libs.saga.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSagaCommand;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSagaCommandRepository extends JpaBaseRepository<JpaSagaCommand, Integer> {

  List<JpaSagaCommand> findBySagaId(Integer sagaId);

  Optional<JpaSagaCommand> findBySagaIdAndName(Integer sagaId, String name);
}
