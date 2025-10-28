package org.atlas.infrastructure.persistence.jpa.impl.saga.mapper;

import org.atlas.framework.saga.core.entity.SagaCommand;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaCommand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaSagaCommandMapper {

  JpaSagaCommandMapper INSTANCE = Mappers.getMapper(JpaSagaCommandMapper.class);

  SagaCommand toSagaCommandEntity(JpaSagaCommand jpaSagaCommand);

  JpaSagaCommand toJpaSagaCommandEntity(SagaCommand sagaCommand);
}
