package org.atlas.infrastructure.api.server.rest.adapter.product.mapper;

import org.atlas.domain.product.entity.Product;
import org.atlas.infrastructure.api.server.rest.adapter.product.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductResponse toProductResponse(Product product);
}
