package org.atlas.services.order.domain.entity;

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
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.domain.common.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CartEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private String userId;

  @Builder.Default
  private List<CartItem> cartItems = new ArrayList<>();

  public boolean isEmpty() {
    return CollectionUtil.isEmpty(cartItems);
  }

  public BigDecimal getTotalAmount() {
    if (CollectionUtil.isEmpty(cartItems)) {
      return BigDecimal.ZERO;
    }
    return cartItems.stream()
        .map(CartItem::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Helper methods

  public synchronized void addCartItem(String productId, Integer quantity) {
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
              CartItemEntity cartItem = new CartItemEntity();
              Product product = new Product(productId);
              cartItem.setProduct(product);
              cartItem.setQuantity(quantity);
              cartItems.add(cartItem);
            }
        );
  }

  public synchronized void setCartItemQuantity(String productId, Integer quantity) {
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
              CartItemEntity cartItem = new CartItemEntity();
              Product product = new Product(productId);
              cartItem.setProduct(product);
              cartItem.setQuantity(quantity);
              cartItems.add(cartItem);
            }
        );
  }

  public void removeCartItem(String productId) {
    if (CollectionUtil.isNotEmpty(cartItems)) {
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

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class CartItem {

    private Integer id;
    private Product product;
    private Integer quantity;

    public BigDecimal getAmount() {
      return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class Product {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;

    public Product(Integer id) {
      this.id = id;
    }
  }
}
