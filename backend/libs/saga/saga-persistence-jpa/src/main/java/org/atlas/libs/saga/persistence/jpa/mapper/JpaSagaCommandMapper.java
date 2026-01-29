package org.atlas.libs.saga.persistence.jpa.mapper;

import org.atlas.libs.framework.saga.core.entity.SagaCommandEntity;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSagaCommand;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaSagaCommandMapper {

  JpaSagaCommandMapper INSTANCE = Mappers.getMapper(JpaSagaCommandMapper.class);

  SagaCommandEntity toSagaCommandEntity(JpaSagaCommand jpaSagaCommand);

  JpaSagaCommand toJpaSagaCommandEntity(SagaCommandEntity sagaCommand);
}
