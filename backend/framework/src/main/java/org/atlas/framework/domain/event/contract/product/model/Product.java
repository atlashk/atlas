package org.atlas.framework.domain.event.contract.product.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {

  private Integer id;
  private String name;
  private BigDecimal price;
}
