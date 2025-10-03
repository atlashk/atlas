package org.atlas.framework.saga.repository;

import java.util.Optional;
import org.atlas.framework.saga.entity.SagaCommandEntity;

public interface SagaCommandRepository {

  Optional<SagaCommandEntity> findBySagaIdAndName(Long sagaId, String name);

  void insert(SagaCommandEntity step);

  void update(SagaCommandEntity step);
}
