package org.atlas.services.product.infrastructure.persistence.jpa.mapper;

import org.atlas.services.product.domain.entity.Brand;
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

  JpaBrand toJpaBrand(Brand brand);

  Brand toBrand(JpaBrand jpaBrand);
}
