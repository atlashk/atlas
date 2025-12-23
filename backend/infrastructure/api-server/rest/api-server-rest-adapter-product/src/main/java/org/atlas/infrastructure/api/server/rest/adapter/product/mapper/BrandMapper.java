package org.atlas.infrastructure.api.server.rest.adapter.product.mapper;

import org.atlas.domain.product.entity.Brand;
import org.atlas.infrastructure.api.server.rest.adapter.product.model.BrandResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

  BrandMapper INSTANCE = Mappers.getMapper(BrandMapper.class);

  BrandResponse toBrandResponse(Brand brand);
}
