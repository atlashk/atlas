package org.atlas.infrastructure.api.server.rest.adapter.user.mapper;

import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.entity.CartItem;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.CartResponse;
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
