package org.atlas.framework.domain.event.contract.order.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

  private Product product;
  private Integer quantity;

  // Copy constructor
  public OrderItem(OrderItem other) {
    this.product = new Product(other.product);
    this.quantity = other.quantity;
  }
}
