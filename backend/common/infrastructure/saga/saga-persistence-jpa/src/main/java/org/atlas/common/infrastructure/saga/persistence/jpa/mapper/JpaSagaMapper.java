package org.atlas.common.infrastructure.saga.persistence.jpa.mapper;

import org.atlas.common.framework.saga.core.entity.SagaEntity;
import org.atlas.common.infrastructure.saga.persistence.jpa.entity.JpaSaga;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaSagaMapper {

  JpaSagaMapper INSTANCE = Mappers.getMapper(JpaSagaMapper.class);

  SagaEntity toSagaEntity(JpaSaga jpaSaga);

  JpaSaga toJpaSagaEntity(SagaEntity saga);
}
