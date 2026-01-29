package org.atlas.services.payment.api.server.rest.front.model;

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
  private String code;
  private String name;
}
