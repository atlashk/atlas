package org.atlas.infrastructure.persistence.jpa.impl.user.mapper;

import lombok.experimental.UtilityClass;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.entity.CartItemEntity;
import org.atlas.domain.user.entity.ProductEntity;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartEntity;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartItemEntity;

@UtilityClass
public class JpaCartEntityMapper {

  public static JpaCartEntity toJpaCartEntity(final CartEntity cart) {
    // Cart
    final JpaCartEntity jpaCart = new JpaCartEntity();
    jpaCart.setId(cart.getId());
    jpaCart.setUserId(cart.getUserId());

    // Cart items
    cart.getCartItems().forEach(cartItem -> {
      JpaCartItemEntity jpaCartItem = new JpaCartItemEntity();
      jpaCartItem.setProductId(cartItem.getProduct().getId());
      jpaCartItem.setQuantity(cartItem.getQuantity());
      jpaCart.addCartItem(jpaCartItem);
    });

    return jpaCart;
  }

  public static CartEntity toCartEntity(final JpaCartEntity jpaCart) {
    // Cart
    final CartEntity cart = new CartEntity(jpaCart.getUserId());
    cart.setId(jpaCart.getId());

    // Cart items
    if (CollectionUtil.isNotEmpty(jpaCart.getCartItems())) {
      jpaCart.getCartItems().forEach(jpaCartItem -> {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setId(jpaCartItem.getId());
        cartItem.setQuantity(jpaCartItem.getQuantity());

        // Product
        ProductEntity product = new ProductEntity();
        product.setId(jpaCartItem.getProductId());
        cartItem.setProduct(product);

        cart.addCartItem(cartItem);
      });
    }

    return cart;
  }
}
