package org.atlas.infrastructure.persistence.jpa.impl.saga;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.saga.core.entity.SagaCommand;
import org.atlas.framework.saga.core.repository.SagaCommandRepository;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaCommand;
import org.atlas.infrastructure.persistence.jpa.impl.saga.mapper.JpaSagaCommandMapper;
import org.atlas.infrastructure.persistence.jpa.impl.saga.repository.JpaSagaCommandRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSagaCommandRepositoryAdapter implements SagaCommandRepository {

  private final JpaSagaCommandRepository jpaSagaCommandRepository;

  @Override
  public List<SagaCommand> findBySagaId(Integer sagaId) {
    List<JpaSagaCommand> jpaSagaCommands = jpaSagaCommandRepository.findBySagaId(sagaId);
    return ObjectMapperUtil.mapList(jpaSagaCommands,
        JpaSagaCommandMapper.INSTANCE::toSagaCommandEntity);
  }

  @Override
  public Optional<SagaCommand> findBySagaIdAndName(Integer sagaId, String name) {
    return jpaSagaCommandRepository.findBySagaIdAndName(sagaId, name)
        .map(JpaSagaCommandMapper.INSTANCE::toSagaCommandEntity);
  }

  @Override
  public void insert(SagaCommand sagaCommand) {
    JpaSagaCommand jpaSagaCommand =
        JpaSagaCommandMapper.INSTANCE.toJpaSagaCommandEntity(sagaCommand);
    jpaSagaCommandRepository.insert(jpaSagaCommand);
    sagaCommand.setId(jpaSagaCommand.getId());
  }

  @Override
  public void update(SagaCommand sagaCommand) {
    JpaSagaCommand jpaSagaCommand =
        JpaSagaCommandMapper.INSTANCE.toJpaSagaCommandEntity(sagaCommand);
    jpaSagaCommandRepository.save(jpaSagaCommand);
  }
}
