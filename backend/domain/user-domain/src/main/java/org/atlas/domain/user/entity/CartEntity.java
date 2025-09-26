package org.atlas.domain.user.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;
import org.atlas.framework.util.CollectionUtil;

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
  public boolean isEmpty() {
    return CollectionUtil.isEmpty(cartItems);
  }

  public synchronized void putCartItem(Integer productId, Integer quantity) {
    if (isEmpty()) {
      cartItems = new ArrayList<>();
    }
    cartItems.stream()
        .filter(it -> it.getProduct().getId().equals(productId))
        .findFirst()
        .ifPresentOrElse(
            it -> it.setQuantity(quantity),
            () -> {
              // Add new cart item
              CartItemEntity cartItem = new CartItemEntity();
              ProductEntity product = new ProductEntity();
              product.setId(productId);
              cartItem.setProduct(product);
              cartItem.setQuantity(quantity);
              cartItems.add(cartItem);
            }
        );
  }

  public void removeCartItem(Integer productId) {
    if (!isEmpty()) {
      Iterator<CartItemEntity> iterator = cartItems.iterator();
      while (iterator.hasNext()) {
        CartItemEntity cartItemEntity = iterator.next();
        if (cartItemEntity.getProduct().getId().equals(productId)) {
          iterator.remove();
          break;
        }
      }
    }
  }

  public void clearCart() {
    if (!isEmpty()) {
      cartItems.clear();
    }
  }

  public List<Integer> getProductIds() {
    return cartItems.stream()
        .map(cartItemEntity -> cartItemEntity.getProduct().getId())
        .distinct()
        .toList();
  }

  public BigDecimal getTotalAmount() {
    return cartItems.stream()
        .map(CartItemEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
