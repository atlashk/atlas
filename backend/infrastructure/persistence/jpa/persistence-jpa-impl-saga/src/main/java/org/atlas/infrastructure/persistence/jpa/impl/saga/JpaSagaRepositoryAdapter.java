package org.atlas.infrastructure.persistence.jpa.impl.saga;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.saga.core.entity.Saga;
import org.atlas.framework.saga.core.repository.SagaRepository;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSaga;
import org.atlas.infrastructure.persistence.jpa.impl.saga.mapper.JpaSagaMapper;
import org.atlas.infrastructure.persistence.jpa.impl.saga.repository.JpaSagaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSagaRepositoryAdapter implements SagaRepository {

  private final JpaSagaRepository jpaSagaRepository;

  @Override
  public Optional<Saga> findById(Integer sagaId) {
    return jpaSagaRepository.findById(sagaId)
        .map(JpaSagaMapper.INSTANCE::toSagaEntity);
  }

  @Override
  public void insert(Saga saga) {
    JpaSaga jpaSaga = JpaSagaMapper.INSTANCE.toJpaSagaEntity(saga);
    jpaSagaRepository.insert(jpaSaga);
    saga.setId(jpaSaga.getId());
  }

  @Override
  public void update(Saga saga) {
    JpaSaga jpaSaga = JpaSagaMapper.INSTANCE.toJpaSagaEntity(saga);
    jpaSagaRepository.save(jpaSaga);
  }
}
