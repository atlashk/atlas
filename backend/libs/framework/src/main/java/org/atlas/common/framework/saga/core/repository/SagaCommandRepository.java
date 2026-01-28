package org.atlas.common.framework.saga.core.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.common.framework.saga.core.entity.SagaCommandEntity;

public interface SagaCommandRepository {

  List<SagaCommandEntity> findBySagaId(Integer sagaId);

  Optional<SagaCommandEntity> findBySagaIdAndName(Integer sagaId, String name);

  void insert(SagaCommandEntity sagaCommand);

  void update(SagaCommandEntity sagaCommand);
}
