package org.atlas.services.order.api.rest.cart.mapper;

import java.util.List;
import org.atlas.services.order.api.rest.cart.model.CartResponse;
import org.atlas.services.order.domain.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

  CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

  // Entity/Output -> Response
  // -----------------------------------------------------------------------------------------------

  default CartResponse toCartResponse(List<CartItemEntity> cartItems) {
    return CartResponse.builder()
        .cartItems(toCartItems(cartItems))
        .totalAmount(CartItemEntity.totalAmount(cartItems))
        .build();
  }

  List<CartResponse.CartItem> toCartItems(List<CartItemEntity> cartItems);

  CartResponse.CartItem toCartItem(CartItemEntity cartItem);

  CartResponse.Product toProduct(CartItemEntity.Product product);
}
