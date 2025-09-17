package org.atlas.framework.domain.event.contract.order.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {

  private Integer id;
  private String name;
  private BigDecimal price;

  // Copy constructor
  public Product(Product other) {
    this.id = other.id;
    this.name = other.name;
    this.price = other.price;
  }
}
