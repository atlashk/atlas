package org.atlas.infrastructure.api.server.rest.impl.product.front.mapper;

import org.atlas.domain.product.entity.Product;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {

  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductResponse toProductResponse(Product product);
}
