package org.atlas.services.catalog.api.rest.brand.mapper;

import org.atlas.services.catalog.api.rest.brand.model.BrandResponse;
import org.atlas.services.catalog.domain.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

  BrandMapper INSTANCE = Mappers.getMapper(BrandMapper.class);

  // Entity/Output --> Response
  // -----------------------------------------------------------------------------------------------

  BrandResponse toBrandResponse(Brand brand);
}
