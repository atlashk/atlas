package org.atlas.infrastructure.persistence.jpa.impl.saga.mapper;

import org.atlas.framework.saga.core.entity.SagaEntity;
import org.atlas.infrastructure.persistence.jpa.impl.saga.entity.JpaSaga;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaSagaMapper {

  JpaSagaMapper INSTANCE = Mappers.getMapper(JpaSagaMapper.class);

  SagaEntity toSagaEntity(JpaSaga jpaSaga);

  JpaSaga toJpaSagaEntity(SagaEntity saga);
}
