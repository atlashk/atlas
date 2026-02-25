package org.atlas.services.order.api.rest.cart.mapper;

import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.api.rest.cart.model.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  // Entity/Output -> Response
  // -----------------------------------------------------------------------------------------------

  CartResponse toCartResponse(CartEntity cart);

  // Don't remove it
  CartResponse.CartItem toCartItem(CartEntity.CartItem cartItem);

  // Don't remove it
  CartResponse.Product toProduct(CartEntity.Product product);
}
