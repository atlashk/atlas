package org.atlas.infrastructure.api.server.rest.impl.user.front.mapper;

import org.atlas.domain.user.entity.Cart;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  CartResponse toCartResponse(Cart entity);

  // Don't remove it
  CartResponse.CartItem toCartItem(Cart.CartItem entity);

  // Don't remove it
  CartResponse.Product toProduct(Cart.Product entity);
}
