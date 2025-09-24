package org.atlas.domain.user.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CartEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private Integer userId;

  private List<CartItemEntity> cartItems;

  public CartEntity(Integer userId) {
    this.userId = userId;
    this.cartItems = new ArrayList<>();
  }

  // Helper methods
  public synchronized void addCartItem(CartItemEntity cartItem) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }
    cartItems.add(cartItem);
    cartItem.setCartId(this.id);
  }

  public void removeCartItem(CartItemEntity cartItem) {
    if (cartItems != null) {
      cartItems.remove(cartItem);
    }
  }

  public void clearItems() {
    if (cartItems != null) {
      cartItems.clear();
    }
  }

  public List<Integer> getProductIds() {
    return cartItems.stream()
        .map(CartItemEntity::getProductId)
        .distinct()
        .toList();
  }
}