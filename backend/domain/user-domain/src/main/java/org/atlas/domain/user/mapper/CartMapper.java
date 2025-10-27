package org.atlas.domain.user.mapper;

import org.atlas.domain.user.entity.CartEntity.Product;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  Product toProduct(ProductResponse response);
}
