package org.atlas.services.order.infrastructure.persistence.jpa.mapper;

import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartItemEntity;
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
  JpaCartEntity toJpaCart(CartEntity cart);

  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "productId", source = "product.id")
  JpaCartItemEntity toJpaCartItem(CartEntity.CartItem cartItem);

  /**
   * After mapping for {@link CartEntity} to {@link JpaCartEntity} - handles bidirectional relationships
   */
  @AfterMapping
  default void afterToJpaCart(@MappingTarget JpaCartEntity jpaCart, CartEntity cart) {
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cart.getCartItems().forEach(cartItem -> {
        JpaCartItemEntity jpaCartItem = toJpaCartItem(cartItem);
        // Bidirectional handling
        jpaCart.addCartItem(jpaCartItem);
      });
    }
  }

  CartEntity toCart(JpaCartEntity jpaCart);

  @Mapping(target = "product.id", source = "productId")
  CartEntity.CartItem toCartItem(JpaCartItemEntity jpaCartItem);
}
