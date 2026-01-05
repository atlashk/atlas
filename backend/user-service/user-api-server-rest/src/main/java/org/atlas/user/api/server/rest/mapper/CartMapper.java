package org.atlas.user.api.server.rest.mapper;

import org.atlas.user.domain.entity.Cart;
import org.atlas.user.domain.entity.CartItem;
import org.atlas.user.api.server.rest.model.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  CartResponse toCartResponse(Cart cart);

  // Don't remove it
  CartResponse.CartItem toCartItem(CartItem cartItem);

  // Don't remove it
  CartResponse.Product toProduct(CartItem.Product product);
}
