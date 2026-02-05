package org.atlas.services.order.infrastructure.persistence.jpa.mapper;

import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCart;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartItem;
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
  JpaCart toJpaCart(CartEntity cart);

  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "productId", source = "product.productId")
  JpaCartItem toJpaCartItem(CartEntity.CartItem cartItem);

  /**
   * After mapping for {@link CartEntity} to {@link JpaCart} - handles bidirectional relationships
   */
  @AfterMapping
  default void afterToJpaCart(@MappingTarget JpaCart jpaCart, CartEntity cart) {
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cart.getCartItems().forEach(cartItem -> {
        JpaCartItem jpaCartItem = toJpaCartItem(cartItem);
        // Bidirectional handling
        jpaCart.addCartItem(jpaCartItem);
      });
    }
  }

  CartEntity toCart(JpaCart jpaCart);

  @Mapping(target = "product.productId", source = "productId")
  CartEntity.CartItem toCartItem(JpaCartItem jpaCartItem);
}
