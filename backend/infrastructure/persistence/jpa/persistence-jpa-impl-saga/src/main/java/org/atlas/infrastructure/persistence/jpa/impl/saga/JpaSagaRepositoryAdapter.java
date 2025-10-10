package org.atlas.infrastructure.persistence.jpa.impl.saga;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaEntity;
import org.atlas.infrastructure.persistence.jpa.impl.saga.repository.JpaSagaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSagaRepositoryAdapter implements SagaRepository {

  private final JpaSagaRepository jpaSagaRepository;

  @Override
  public Optional<SagaEntity> findById(Integer sagaId) {
    return jpaSagaRepository.findById(sagaId)
        .map(jpaSaga -> ObjectMapperUtil.getInstance()
            .map(jpaSaga, SagaEntity.class));
  }

  @Override
  public void insert(SagaEntity saga) {
    JpaSagaEntity jpaSaga = ObjectMapperUtil.getInstance()
        .map(saga, JpaSagaEntity.class);
    jpaSagaRepository.insert(jpaSaga);
    saga.setId(jpaSaga.getId());
  }

  @Override
  public void update(SagaEntity saga) {
    JpaSagaEntity jpaSaga = ObjectMapperUtil.getInstance()
        .map(saga, JpaSagaEntity.class);
    jpaSagaRepository.save(jpaSaga);
  }
}
