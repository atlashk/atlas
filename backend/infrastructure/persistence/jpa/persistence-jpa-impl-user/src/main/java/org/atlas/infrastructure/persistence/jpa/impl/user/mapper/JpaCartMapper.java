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

  /**
   * After mapping for Cart to JpaCart - handle cart items
   */
  @AfterMapping
  default void afterToJpaCart(@MappingTarget JpaCart jpaCart, Cart cart) {
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cart.getCartItems().forEach(cartItem -> {
        JpaCartItem jpaCartItem = toJpaCartItem(cartItem);
        jpaCart.addCartItem(jpaCartItem);
      });
    }
  }

  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "productId", source = "product.id")
  JpaCartItem toJpaCartItem(Cart.CartItem cartItem);

  @Mapping(target = "cartItems", ignore = true)
  Cart toCart(JpaCart jpaCart);

  @Mapping(target = "product", ignore = true)
  Cart.CartItem toCartItem(JpaCartItem jpaCartItem);

  /**
   * After mapping for JpaCart to Cart - handle cart items
   */
  @AfterMapping
  default void afterToCart(@MappingTarget Cart cart, JpaCart jpaCart) {
    if (CollectionUtil.isNotEmpty(jpaCart.getCartItems())) {
      jpaCart.getCartItems().forEach(jpaCartItem -> {
        Cart.CartItem cartItem = toCartItem(jpaCartItem);

        // Create product with only ID
        Cart.Product product = new Cart.Product();
        product.setId(jpaCartItem.getProductId());
        cartItem.setProduct(product);

        cart.addCartItem(cartItem);
      });
    }
  }
}
