package org.atlas.framework.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.entity.SagaCommandEntity;

public interface SagaCommandRepository {

  List<SagaCommandEntity> findBySagaId(Integer sagaId);

  Optional<SagaCommandEntity> findBySagaIdAndName(Integer sagaId, String name);

  void insert(SagaCommandEntity sagaCommand);

  void update(SagaCommandEntity sagaCommand);
}
