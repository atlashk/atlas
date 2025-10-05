package org.atlas.framework.saga.repository;

import java.util.Optional;
import org.atlas.framework.saga.entity.SagaEntity;

public interface SagaRepository {

  Optional<SagaEntity> findById(Long sagaId);

  void insert(SagaEntity entity);

  void update(SagaEntity entity);
}
