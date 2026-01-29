package org.atlas.libs.saga.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.saga.core.entity.SagaCommandEntity;
import org.atlas.libs.framework.saga.core.repository.SagaCommandRepository;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSagaCommand;
import org.atlas.libs.saga.persistence.jpa.mapper.JpaSagaCommandMapper;
import org.atlas.libs.saga.persistence.jpa.repository.JpaSagaCommandRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSagaCommandRepositoryAdapter implements SagaCommandRepository {

  private final JpaSagaCommandRepository jpaSagaCommandRepository;

  @Override
  public List<SagaCommandEntity> findBySagaId(Integer sagaId) {
    List<JpaSagaCommand> jpaSagaCommands = jpaSagaCommandRepository.findBySagaId(sagaId);
    return ObjectMapperUtil.mapList(jpaSagaCommands,
        JpaSagaCommandMapper.INSTANCE::toSagaCommandEntity);
  }

  @Override
  public Optional<SagaCommandEntity> findBySagaIdAndName(Integer sagaId, String name) {
    return jpaSagaCommandRepository.findBySagaIdAndName(sagaId, name)
        .map(JpaSagaCommandMapper.INSTANCE::toSagaCommandEntity);
  }

  @Override
  public void insert(SagaCommandEntity sagaCommand) {
    JpaSagaCommand jpaSagaCommand =
        JpaSagaCommandMapper.INSTANCE.toJpaSagaCommandEntity(sagaCommand);
    jpaSagaCommandRepository.insert(jpaSagaCommand);
    sagaCommand.setId(jpaSagaCommand.getId());
  }

  @Override
  public void update(SagaCommandEntity sagaCommand) {
    JpaSagaCommand jpaSagaCommand =
        JpaSagaCommandMapper.INSTANCE.toJpaSagaCommandEntity(sagaCommand);
    jpaSagaCommandRepository.save(jpaSagaCommand);
  }
}
