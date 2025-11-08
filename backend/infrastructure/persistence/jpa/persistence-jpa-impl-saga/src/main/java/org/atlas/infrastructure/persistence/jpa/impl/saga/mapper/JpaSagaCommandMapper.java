package org.atlas.infrastructure.persistence.jpa.impl.saga.mapper;

import org.atlas.framework.saga.core.entity.SagaCommandEntity;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSagaCommand;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface JpaSagaCommandMapper {

  JpaSagaCommandMapper INSTANCE = Mappers.getMapper(JpaSagaCommandMapper.class);

  SagaCommandEntity toSagaCommandEntity(JpaSagaCommand jpaSagaCommand);

  JpaSagaCommand toJpaSagaCommandEntity(SagaCommandEntity sagaCommand);
}
