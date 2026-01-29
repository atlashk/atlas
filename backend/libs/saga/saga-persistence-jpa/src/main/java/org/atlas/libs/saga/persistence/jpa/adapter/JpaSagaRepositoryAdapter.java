package org.atlas.libs.saga.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.saga.core.entity.SagaEntity;
import org.atlas.libs.framework.saga.core.repository.SagaRepository;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSaga;
import org.atlas.libs.saga.persistence.jpa.mapper.JpaSagaMapper;
import org.atlas.libs.saga.persistence.jpa.repository.JpaSagaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSagaRepositoryAdapter implements SagaRepository {

  private final JpaSagaRepository jpaSagaRepository;

  @Override
  public Optional<SagaEntity> findById(Integer sagaId) {
    return jpaSagaRepository.findById(sagaId)
        .map(JpaSagaMapper.INSTANCE::toSagaEntity);
  }

  @Override
  public void insert(SagaEntity saga) {
    JpaSaga jpaSaga = JpaSagaMapper.INSTANCE.toJpaSagaEntity(saga);
    jpaSagaRepository.insert(jpaSaga);
    saga.setId(jpaSaga.getId());
  }

  @Override
  public void update(SagaEntity saga) {
    JpaSaga jpaSaga = JpaSagaMapper.INSTANCE.toJpaSagaEntity(saga);
    jpaSagaRepository.save(jpaSaga);
  }
}
