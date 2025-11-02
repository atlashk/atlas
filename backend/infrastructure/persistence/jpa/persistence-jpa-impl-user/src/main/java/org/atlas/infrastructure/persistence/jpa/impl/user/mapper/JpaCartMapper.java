package org.atlas.infrastructure.persistence.jpa.impl.user.mapper;

import org.atlas.domain.user.entity.Cart;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCart;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JpaCartMapper {

  JpaCartMapper INSTANCE = Mappers.getMapper(JpaCartMapper.class);

  @Mapping(target = "cartItems", ignore = true)
  JpaCart toJpaCart(Cart cart);

  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "productId", source = "product.id")
  JpaCartItem toJpaCartItem(Cart.CartItem cartItem);

  /**
   * After mapping for Cart to JpaCart
   */
  @AfterMapping
  default void afterToJpaCart(@MappingTarget JpaCart jpaCart, Cart cart) {
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cart.getCartItems().forEach(cartItem -> {
        JpaCartItem jpaCartItem = toJpaCartItem(cartItem);
        // Bidirectional handling
        jpaCart.addCartItem(jpaCartItem);
      });
    }
  }

  Cart toCart(JpaCart jpaCart);

  @Mapping(target = "product.id", source = "productId")
  Cart.CartItem toOrderItem(JpaCartItem jpaCartItem);
}
