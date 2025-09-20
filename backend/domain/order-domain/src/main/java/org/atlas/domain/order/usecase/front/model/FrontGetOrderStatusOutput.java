package org.atlas.domain.order.usecase.front.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.order.shared.OrderStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontGetOrderStatusOutput {

  private OrderStatus status;
  private String cancellationReason;
}
