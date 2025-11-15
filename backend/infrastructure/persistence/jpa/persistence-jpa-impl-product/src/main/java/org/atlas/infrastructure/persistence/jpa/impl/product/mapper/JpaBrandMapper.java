package org.atlas.infrastructure.persistence.jpa.impl.product.mapper;

import org.atlas.domain.product.entity.Brand;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaBrand;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaBrandMapper {

  JpaBrandMapper INSTANCE = Mappers.getMapper(JpaBrandMapper.class);

  JpaBrand toJpaBrand(Brand brand);

  Brand toBrand(JpaBrand jpaBrand);
}
