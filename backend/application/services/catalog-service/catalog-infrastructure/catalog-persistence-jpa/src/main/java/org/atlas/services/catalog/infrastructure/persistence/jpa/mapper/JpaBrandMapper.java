package org.atlas.services.catalog.infrastructure.persistence.jpa.mapper;

import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaBrandEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaBrandMapper {

  JpaBrandMapper INSTANCE = Mappers.getMapper(JpaBrandMapper.class);

  JpaBrandEntity toJpaBrand(BrandEntity brand);

  BrandEntity toBrand(JpaBrandEntity jpaBrand);
}
