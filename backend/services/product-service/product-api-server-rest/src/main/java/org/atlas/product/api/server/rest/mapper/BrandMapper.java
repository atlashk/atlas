package org.atlas.product.api.server.rest.mapper;

import org.atlas.product.domain.entity.Brand;
import org.atlas.product.api.server.rest.model.BrandResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

  BrandMapper INSTANCE = Mappers.getMapper(BrandMapper.class);

  BrandResponse toBrandResponse(Brand brand);
}
