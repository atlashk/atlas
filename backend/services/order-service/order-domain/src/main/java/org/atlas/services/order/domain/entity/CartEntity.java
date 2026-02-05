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
import org.atlas.libs.framework.util.CollectionUtil;
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
        .filter(it -> it.getProduct().getProductId().equals(productId))
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

  public synchronized void setCartItemQuantity(String productId, Integer quantity) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }
    cartItems.stream()
        .filter(it -> it.getProduct().getProductId().equals(productId))
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

  public void removeCartItem(String productId) {
    if (CollectionUtil.isNotEmpty(cartItems)) {
      Iterator<CartItem> iterator = cartItems.iterator();
      while (iterator.hasNext()) {
        CartItem cartItem = iterator.next();
        if (cartItem.getProduct().getProductId().equals(productId)) {
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

  public List<String> collectProductIds() {
    if (CollectionUtil.isEmpty(cartItems)) {
      return java.util.Collections.emptyList();
    }
    return cartItems.stream()
        .map(it -> it.getProduct().getProductId())
        .toList();
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class CartItem {

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

    private String productId;
    private String name;
    private BigDecimal price;
    private String image;

    public Product(String productId) {
      this.productId = productId;
    }
  }
}
