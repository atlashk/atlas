package org.atlas.framework.domain.event.contract.order.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

  private Integer productId;
  private Integer quantity;
}
