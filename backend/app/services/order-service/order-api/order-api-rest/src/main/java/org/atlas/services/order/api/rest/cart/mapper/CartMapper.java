package org.atlas.services.order.api.rest.cart.mapper;

import java.util.List;
import org.atlas.services.order.api.rest.cart.model.CartResponse;
import org.atlas.services.order.domain.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  // Entity/Output -> Response
  // -----------------------------------------------------------------------------------------------

  default CartResponse toCartResponse(List<CartItem> cartItems) {
    return CartResponse.builder()
        .cartItems(toCartItems(cartItems))
        .totalAmount(CartItem.totalAmount(cartItems))
        .build();
  }

  List<CartResponse.CartItem> toCartItems(List<CartItem> cartItems);

  CartResponse.CartItem toCartItem(CartItem cartItem);

  CartResponse.Product toProduct(CartItem.Product product);
}
