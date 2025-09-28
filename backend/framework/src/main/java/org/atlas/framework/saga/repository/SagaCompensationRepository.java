package org.atlas.framework.saga.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.framework.saga.entity.SagaCompensationEntity;
import org.atlas.framework.saga.entity.SagaCompensationStatus;

public interface SagaCompensationRepository {

  Optional<SagaCompensationEntity> findById(Long compensationId);

  List<SagaCompensationEntity> findBySagaId(Long sagaId);

  List<SagaCompensationEntity> findByStepId(Long stepId);

  List<SagaCompensationEntity> findByStatus(SagaCompensationStatus status);

  List<SagaCompensationEntity> findBySagaIdAndStatus(Long sagaId, SagaCompensationStatus status);

  List<SagaCompensationEntity> findByStatusAndRetryCountLessThanMaxRetries();

  Optional<SagaCompensationEntity> findBySagaIdAndStepId(Long sagaId, Long stepId);

  void insert(SagaCompensationEntity compensation);

  void update(SagaCompensationEntity compensation);

  void delete(Long compensationId);

  boolean existsById(Long compensationId);
}
