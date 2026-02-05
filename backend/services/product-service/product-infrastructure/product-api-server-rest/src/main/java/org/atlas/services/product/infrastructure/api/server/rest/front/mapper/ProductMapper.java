package org.atlas.services.product.infrastructure.api.server.rest.front.mapper;

import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.infrastructure.api.server.rest.front.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductResponse toProductResponse(ProductEntity product);
}
