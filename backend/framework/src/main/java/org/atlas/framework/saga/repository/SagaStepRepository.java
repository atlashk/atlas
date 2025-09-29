package org.atlas.framework.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.entity.SagaStepEntity;
import org.atlas.framework.saga.entity.SagaStepStatus;

public interface SagaStepRepository {

  List<SagaStepEntity> findCompleted(Long sagaId);

  Optional<SagaStepEntity> findById(Long stepId);

  Optional<SagaStepEntity> findLastStep(Long sagaId);

  void insert(SagaStepEntity step);

  void update(SagaStepEntity step);

  void delete(Long stepId);
}
