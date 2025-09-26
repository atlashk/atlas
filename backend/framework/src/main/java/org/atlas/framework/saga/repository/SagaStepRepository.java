package org.atlas.framework.saga.repository;

import java.util.Optional;
import org.atlas.framework.saga.entity.SagaStepEntity;

public interface SagaStepRepository {

  Optional<SagaStepEntity> findById(Long stepId);

  void insert(SagaStepEntity step);

  void update(SagaStepEntity step);
}
