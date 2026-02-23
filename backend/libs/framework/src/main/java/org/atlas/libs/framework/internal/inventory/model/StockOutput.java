package org.atlas.libs.framework.internal.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.identity.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOutput {

  private String productId;

  private Integer availableQuantity;

  private Integer reservedQuantity;
}
