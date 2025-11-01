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
public class Cart extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private Integer userId;

  private List<CartItem> cartItems = new ArrayList<>();

  public Cart(Integer userId) {
    this.userId = userId;
  }

  public BigDecimal getTotalAmount() {
    if (!hasItems()) {
      return BigDecimal.ZERO;
    }
    return cartItems.stream()
        .map(CartItem::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  // Helper methods
  public boolean hasItems() {
    return CollectionUtil.isNotEmpty(cartItems);
  }

  public void addCartItem(CartItem cartItem) {
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
              CartItem cartItem = new CartItem();
              Product product = new Product(productId);
              cartItem.setProduct(product);
              cartItem.setQuantity(quantity);
              cartItems.add(cartItem);
            }
        );
  }

  public void removeCartItem(Integer productId) {
    if (hasItems()) {
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

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;

    public Product(Integer id) {
      this.id = id;
    }
  }
}
