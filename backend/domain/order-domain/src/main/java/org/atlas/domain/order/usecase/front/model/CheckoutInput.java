package org.atlas.domain.order.usecase.front.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.entity.OrderEntity.Address;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CheckoutInput {

  private Integer userId;
  private Address address;
  private Integer paymentGatewayId;
}
