package org.atlas.infrastructure.persistence.jpa.adapter.saga.mapper;

import org.atlas.framework.saga.core.entity.SagaCommandEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.saga.entity.JpaSagaCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaSagaCommandMapper {

  JpaSagaCommandMapper INSTANCE = Mappers.getMapper(JpaSagaCommandMapper.class);

  SagaCommandEntity toSagaCommandEntity(JpaSagaCommand jpaSagaCommand);

  JpaSagaCommand toJpaSagaCommandEntity(SagaCommandEntity sagaCommand);
}
