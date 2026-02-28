package org.atlas.services.inventory.port.in.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StockOutput {

  private String productId;

  private Integer availableQuantity;

  private Integer reservedQuantity;
}
