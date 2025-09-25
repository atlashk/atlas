package org.atlas.infrastructure.persistence.jpa.impl.user.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.entity.CartItemEntity;
import org.atlas.domain.user.entity.ProductEntity;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartEntity;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartItemEntity;

@UtilityClass
public class JpaCartEntityMapper {

  public static JpaCartEntity toJpaCartEntity(final CartEntity cartEntity) {
    // Cart
    final JpaCartEntity jpaCartEntity = new JpaCartEntity();
    jpaCartEntity.setId(cartEntity.getId());
    jpaCartEntity.setUserId(cartEntity.getUserId());

    // Cart items
    cartEntity.getCartItems().forEach(cartItemEntity -> {
      JpaCartItemEntity jpaCartItemEntity = new JpaCartItemEntity();
      jpaCartItemEntity.setProductId(cartItemEntity.getProduct().getId());
      jpaCartItemEntity.setQuantity(cartItemEntity.getQuantity());
      jpaCartEntity.addCartItem(jpaCartItemEntity);
    });

    return jpaCartEntity;
  }

  public static CartEntity toCartEntity(final JpaCartEntity jpaCartEntity) {
    // Cart
    final CartEntity cartEntity = new CartEntity(jpaCartEntity.getUserId());
    cartEntity.setId(jpaCartEntity.getId());

    // Cart items
    if (jpaCartEntity.getCartItems() != null) {
      jpaCartEntity.getCartItems().forEach(jpaCartItemEntity -> {
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setId(jpaCartItemEntity.getId());
        cartItemEntity.setQuantity(jpaCartItemEntity.getQuantity());

        // Product
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(jpaCartItemEntity.getProductId());
        cartItemEntity.setProduct(productEntity);

        cartEntity.addCartItem(cartItemEntity);
      });
    }

    return cartEntity;
  }
}
