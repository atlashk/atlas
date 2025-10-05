package org.atlas.framework.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.entity.SagaCommandEntity;
import org.atlas.framework.saga.command.CheckoutCommand;

public interface SagaCommandRepository {

  List<SagaCommandEntity> findBySagaId(Long sagaId);

  Optional<SagaCommandEntity> findBySagaIdAndName(Long sagaId, String name);

  void insert(SagaCommandEntity entity);

  void update(SagaCommandEntity entity);
}
