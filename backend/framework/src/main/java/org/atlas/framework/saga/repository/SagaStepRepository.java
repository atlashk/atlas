package org.atlas.framework.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.entity.SagaStepEntity;
import org.atlas.framework.saga.entity.SagaStepStatus;

public interface SagaStepRepository {

  Optional<SagaStepEntity> findById(Long stepId);

  List<SagaStepEntity> findBySagaId(Long sagaId);

  List<SagaStepEntity> findBySagaIdOrderByStepOrder(Long sagaId);

  List<SagaStepEntity> findByStatus(SagaStepStatus status);

  List<SagaStepEntity> findBySagaIdAndStatus(Long sagaId, SagaStepStatus status);

  Optional<SagaStepEntity> findBySagaIdAndStepName(Long sagaId, String stepName);

  void insert(SagaStepEntity step);

  void update(SagaStepEntity step);

  void delete(Long stepId);

  boolean existsById(Long stepId);
}
