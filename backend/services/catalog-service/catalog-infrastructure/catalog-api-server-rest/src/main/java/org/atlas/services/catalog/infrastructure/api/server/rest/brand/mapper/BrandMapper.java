package org.atlas.services.catalog.infrastructure.api.server.rest.brand.mapper;

import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.infrastructure.api.server.rest.brand.model.BrandResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

  BrandMapper INSTANCE = Mappers.getMapper(BrandMapper.class);

  // Entity/Output --> Response
  // -----------------------------------------------------------------------------------------------

  BrandResponse toBrandResponse(BrandEntity brand);
}
