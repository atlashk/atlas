package org.atlas.framework.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;

public interface SagaRepository {

  Optional<SagaEntity> findById(Long sagaId);

  List<SagaEntity> findByStatus(SagaStatus status);

  List<SagaEntity> findByOrchestratorName(String orchestratorName);

  List<SagaEntity> findByStatusAndOrchestratorName(SagaStatus status, String orchestratorName);

  void insert(SagaEntity saga);

  void update(SagaEntity saga);

  void delete(Long sagaId);

  boolean existsById(Long sagaId);
}
