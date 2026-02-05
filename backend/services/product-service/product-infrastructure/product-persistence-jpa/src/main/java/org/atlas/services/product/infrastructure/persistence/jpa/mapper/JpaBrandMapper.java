package org.atlas.services.product.infrastructure.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.BrandEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaBrand;
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

  JpaBrand toJpaBrand(BrandEntity brand);

  BrandEntity toBrand(JpaBrand jpaBrand);
}
