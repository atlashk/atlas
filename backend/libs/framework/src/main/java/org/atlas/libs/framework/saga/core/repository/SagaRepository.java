package org.atlas.libs.framework.saga.core.repository;

import java.util.Optional;
import org.atlas.libs.framework.saga.core.entity.SagaEntity;

public interface SagaRepository {

  Optional<SagaEntity> findById(Integer sagaId);

  void insert(SagaEntity saga);

  void update(SagaEntity saga);
}
