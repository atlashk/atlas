package org.atlas.infrastructure.persistence.jpa.impl.saga;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.framework.saga.core.entity.SagaCommandEntity;
import org.atlas.framework.saga.core.repository.SagaCommandRepository;
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
  public void insert(SagaCommandEntity sagaCommand) {
    JpaSagaCommandEntity jpaSagaCommand = ObjectMapperUtil.getInstance()
        .map(sagaCommand, JpaSagaCommandEntity.class);
    jpaSagaCommandRepository.insert(jpaSagaCommand);
    sagaCommand.setId(jpaSagaCommand.getId());
  }

  @Override
  public void update(SagaCommandEntity sagaCommand) {
    JpaSagaCommandEntity jpaSagaCommand = ObjectMapperUtil.getInstance()
        .map(sagaCommand, JpaSagaCommandEntity.class);
    jpaSagaCommandRepository.save(jpaSagaCommand);
  }
}
