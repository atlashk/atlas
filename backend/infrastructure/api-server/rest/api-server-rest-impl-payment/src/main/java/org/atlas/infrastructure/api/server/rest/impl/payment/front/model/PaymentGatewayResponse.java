package org.atlas.infrastructure.api.server.rest.impl.payment.front.model;

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
public class PaymentGatewayResponse {

  private Integer id;
  private String name;
}
