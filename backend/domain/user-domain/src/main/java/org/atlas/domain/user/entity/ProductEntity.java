package org.atlas.domain.user.entity;

import java.math.BigDecimal;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

  private Integer id;
  private String name;
  private BigDecimal price;
  private String image;

  public ProductEntity(Integer id) {
    this.id = id;
  }
}
