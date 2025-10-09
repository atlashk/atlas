package org.atlas.infrastructure.persistence.jpa.impl.saga;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.saga.entity.SagaCommandEntity;
import org.atlas.framework.saga.repository.SagaCommandRepository;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaCommandEntity;
import org.atlas.infrastructure.persistence.jpa.impl.saga.repository.JpaSagaCommandRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSagaCommandRepositoryAdapter implements SagaCommandRepository {

  private final JpaSagaCommandRepository jpaSagaCommandRepository;

  @Override
  public List<SagaCommandEntity> findBySagaId(Integer sagaId) {
    List<JpaSagaCommandEntity> jpaSagaCommands = jpaSagaCommandRepository.findBySagaId(sagaId);
    return ObjectMapperUtil.getInstance()
        .mapList(jpaSagaCommands, SagaCommandEntity.class);
  }

  @Override
  public Optional<SagaCommandEntity> findBySagaIdAndName(Integer sagaId, String name) {
    return jpaSagaCommandRepository.findBySagaIdAndName(sagaId, name)
        .map(jpaSagaCommand -> ObjectMapperUtil.getInstance()
            .map(jpaSagaCommand, SagaCommandEntity.class));
  }

  @Override
  public void insert(SagaCommandEntity entity) {
    JpaSagaCommandEntity jpaSagaCommand = ObjectMapperUtil.getInstance()
        .map(entity, JpaSagaCommandEntity.class);
    jpaSagaCommandRepository.insert(jpaSagaCommand);
    entity.setId(jpaSagaCommand.getId());
  }

  @Override
  public void update(SagaCommandEntity entity) {
    JpaSagaCommandEntity jpaSagaCommand = ObjectMapperUtil.getInstance()
        .map(entity, JpaSagaCommandEntity.class);
    jpaSagaCommandRepository.save(jpaSagaCommand);
  }
}