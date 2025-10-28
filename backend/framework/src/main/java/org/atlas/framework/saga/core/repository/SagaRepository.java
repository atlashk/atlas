package org.atlas.framework.saga.core.repository;

import java.util.Optional;
import org.atlas.framework.saga.core.entity.Saga;

public interface SagaRepository {

  Optional<Saga> findById(Integer sagaId);

  void insert(Saga saga);

  void update(Saga saga);
}
