package org.atlas.domain.order.entity;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductEntity {

  private Integer id;
  private String name;
  private BigDecimal price;
}
