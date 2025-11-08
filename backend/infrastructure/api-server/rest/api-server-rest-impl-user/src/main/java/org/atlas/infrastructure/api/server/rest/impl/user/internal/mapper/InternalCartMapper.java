package org.atlas.infrastructure.api.server.rest.impl.user.internal.mapper;

import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.entity.CartItem;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InternalCartMapper {

  InternalCartMapper INSTANCE = Mappers.getMapper(InternalCartMapper.class);

  CartResponse toCartResponse(Cart cart);

  // Don't remove it
  CartResponse.CartItem toCartItem(CartItem cartItem);

  // Don't remove it
  CartResponse.Product toProduct(CartItem.Product product);
}
