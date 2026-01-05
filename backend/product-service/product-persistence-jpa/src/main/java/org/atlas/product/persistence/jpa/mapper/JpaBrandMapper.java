package org.atlas.product.persistence.jpa.mapper;

import org.atlas.product.domain.entity.Brand;
import org.atlas.product.persistence.jpa.entity.JpaBrand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JpaBrandMapper {

  JpaBrandMapper INSTANCE = Mappers.getMapper(JpaBrandMapper.class);

  JpaBrand toJpaBrand(Brand brand);

  Brand toBrand(JpaBrand jpaBrand);
}
