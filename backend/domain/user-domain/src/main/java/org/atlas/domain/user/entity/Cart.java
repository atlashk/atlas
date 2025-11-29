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
import org.atlas.domain.user.entity.CartItem.Product;
import org.atlas.framework.domain.entity.DomainEntity;
import org.atlas.framework.collection.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Cart extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private Integer userId;

  @Builder.Default
  private List<CartItem> cartItems = new ArrayList<>();

  public BigDecimal getTotalAmount() {
    if (CollectionUtil.isEmpty(cartItems)) {
      return BigDecimal.ZERO;
    }
    return cartItems.stream()
        .map(CartItem::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Helper methods

  public synchronized void addCartItem(Integer productId, Integer quantity) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }
    cartItems.stream()
        .filter(it -> it.getProduct().getId().equals(productId))
        .findFirst()
        .ifPresentOrElse(
            it -> it.setQuantity(it.getQuantity() + quantity),
            () -> {
              // Add new cart item
              CartItem cartItem = new CartItem();
              Product product = new Product(productId);
              cartItem.setProduct(product);
              cartItem.setQuantity(quantity);
              cartItems.add(cartItem);
            }
        );
  }

  public synchronized void setCartItemQuantity(Integer productId, Integer quantity) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }
    cartItems.stream()
        .filter(it -> it.getProduct().getId().equals(productId))
        .findFirst()
        .ifPresentOrElse(
            it -> it.setQuantity(quantity),
            () -> {
              // Add new cart item
              CartItem cartItem = new CartItem();
              Product product = new Product(productId);
              cartItem.setProduct(product);
              cartItem.setQuantity(quantity);
              cartItems.add(cartItem);
            }
        );
  }

  public void removeCartItem(Integer productId) {
    if (CollectionUtil.isNotEmpty(cartItems)) {
      Iterator<CartItem> iterator = cartItems.iterator();
      while (iterator.hasNext()) {
        CartItem cartItemEntity = iterator.next();
        if (cartItemEntity.getProduct().getId().equals(productId)) {
          iterator.remove();
          break;
        }
      }
    }
  }

  public void clear() {
    if (CollectionUtil.isNotEmpty(cartItems)) {
      cartItems.clear();
    }
  }

  public List<Integer> collectProductIds() {
    if (CollectionUtil.isEmpty(cartItems)) {
      return java.util.Collections.emptyList();
    }
    return cartItems.stream()
        .map(it -> it.getProduct().getId())
        .toList();
  }
}
