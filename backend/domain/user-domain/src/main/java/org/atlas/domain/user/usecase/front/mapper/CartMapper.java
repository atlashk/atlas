package org.atlas.domain.user.usecase.front.mapper;

import org.atlas.domain.user.entity.Cart.Product;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  Product toProduct(ProductResponse response);
}
