package org.atlas.framework.saga.core.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.core.entity.SagaCommand;

public interface SagaCommandRepository {

  List<SagaCommand> findBySagaId(Integer sagaId);

  Optional<SagaCommand> findBySagaIdAndName(Integer sagaId, String name);

  void insert(SagaCommand sagaCommand);

  void update(SagaCommand sagaCommand);
}
