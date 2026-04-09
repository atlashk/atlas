package org.atlas.libs.saga.persistence.jpa.mapper;

import org.atlas.libs.framework.saga.core.entity.SagaEntity;
import org.atlas.libs.saga.persistence.jpa.entity.JpaSagaEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaSagaMapper {

  JpaSagaMapper INSTANCE = Mappers.getMapper(JpaSagaMapper.class);

  SagaEntity toSagaEntity(JpaSagaEntity jpaSaga);

  JpaSagaEntity toJpaSagaEntity(SagaEntity saga);

  void merge(SagaEntity saga, @MappingTarget JpaSagaEntity jpaSaga);
}
