package org.atlas.services.user.api.server.rest.internal.mapper;

import org.atlas.libs.framework.internalapi.user.model.CartResponse;
import org.atlas.services.user.domain.entity.Cart;
import org.atlas.services.user.domain.entity.CartItem;
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
