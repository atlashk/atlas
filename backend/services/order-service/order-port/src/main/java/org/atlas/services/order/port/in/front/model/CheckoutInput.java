package org.atlas.services.order.port.in.front.model;

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
public class CheckoutInput {

  private Address address;
  private Integer paymentGatewayId;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Address {

    private String street;
    private String city;
    private String country; // Country code
    private String postalCode;
  }
}
