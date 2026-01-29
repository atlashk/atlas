package org.atlas.services.product.api.server.rest.mapper;

import org.atlas.services.product.api.server.rest.model.ProductResponse;
import org.atlas.services.product.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductResponse toProductResponse(Product product);
}
