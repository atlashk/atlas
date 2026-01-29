package org.atlas.services.user.persistence.jpa.mapper;

import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.user.domain.entity.Cart;
import org.atlas.services.user.domain.entity.CartItem;
import org.atlas.services.user.persistence.jpa.entity.JpaCart;
import org.atlas.services.user.persistence.jpa.entity.JpaCartItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaCartMapper {

  JpaCartMapper INSTANCE = Mappers.getMapper(JpaCartMapper.class);

  @Mapping(target = "cartItems", ignore = true)
  JpaCart toJpaCart(Cart cart);

  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "productId", source = "product.id")
  JpaCartItem toJpaCartItem(CartItem cartItem);

  /**
   * After mapping for {@link Cart} to {@link JpaCart} - handles bidirectional relationships
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
  CartItem toCartItem(JpaCartItem jpaCartItem);
}
