package org.atlas.framework.saga.repository;

import java.util.Optional;
import org.atlas.framework.saga.entity.SagaCompensationEntity;

public interface SagaCompensationRepository {

  Optional<SagaCompensationEntity> findById(Long compensationId);

  void insert(SagaCompensationEntity compensation);

  void update(SagaCompensationEntity compensation);
}
