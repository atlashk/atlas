package org.atlas.domain.order.usecase.front.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GetOrderStatusOutput {

  private OrderStatus status;
  private String cancellationReason;
}
