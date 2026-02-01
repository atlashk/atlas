package org.atlas.services.product.infrastructure.api.server.rest.front.mapper;

import org.atlas.services.product.infrastructure.api.server.rest.front.model.BrandResponse;
import org.atlas.services.product.domain.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

  BrandMapper INSTANCE = Mappers.getMapper(BrandMapper.class);

  BrandResponse toBrandResponse(Brand brand);
}
