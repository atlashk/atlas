package org.atlas.domain.user.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;
import org.atlas.framework.util.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CartEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private Integer userId;

  private List<CartItemEntity> cartItems = new ArrayList<>();

  public CartEntity(Integer userId) {
    this.userId = userId;
  }

  public BigDecimal getTotalAmount() {
    return cartItems.stream()
        .map(CartItemEntity::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Helper methods
  public boolean hasItems() {
    return CollectionUtil.isNotEmpty(cartItems);
  }

  public void addCartItem(CartItemEntity cartItem) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }
    cartItems.add(cartItem);
  }

  public synchronized void putCartItem(Integer productId, Integer quantity) {
    if (!hasItems()) {
      cartItems = new ArrayList<>();
    }
    cartItems.stream()
        .filter(it -> it.getProduct().getId().equals(productId))
        .findFirst()
        .ifPresentOrElse(
            it -> it.setQuantity(it.getQuantity() + quantity),
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
    if (hasItems()) {
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

  public void clear() {
    if (hasItems()) {
      cartItems.clear();
    }
  }

  public List<Integer> collectProductIds() {
    return cartItems.stream()
        .map(cartItemEntity -> cartItemEntity.getProduct().getId())
        .distinct()
        .toList();
  }
}
