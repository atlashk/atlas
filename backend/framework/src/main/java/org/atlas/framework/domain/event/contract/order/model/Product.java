package org.atlas.framework.domain.event.contract.order.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
