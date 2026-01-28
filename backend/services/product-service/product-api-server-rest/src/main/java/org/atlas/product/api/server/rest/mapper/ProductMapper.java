package org.atlas.product.api.server.rest.mapper;

import org.atlas.product.domain.entity.Product;
import org.atlas.product.api.server.rest.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductResponse toProductResponse(Product product);
}
