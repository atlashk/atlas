package org.atlas.services.order.api.server.rest.internal.mapper;

import org.atlas.services.order.domain.entity.Cart;
import org.atlas.services.order.domain.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalCartMapper {

  InternalCartMapper INSTANCE = Mappers.getMapper(InternalCartMapper.class);

  CartResponse toCartResponse(Cart cart);

  // Don't remove it
  CartResponse.CartItem toCartItem(CartItem cartItem);

  // Don't remove it
  CartResponse.Product toProduct(CartItem.Product product);
}
