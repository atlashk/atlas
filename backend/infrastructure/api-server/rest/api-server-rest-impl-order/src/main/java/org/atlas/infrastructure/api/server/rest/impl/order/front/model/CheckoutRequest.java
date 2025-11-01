package org.atlas.infrastructure.api.server.rest.impl.order.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request object for placing a new order")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CheckoutRequest {

  @NotNull
  @Schema(description = "Delivery or billing address information", requiredMode = RequiredMode.REQUIRED)
  private @Valid Address address;

  @Schema(description = "The identifier of payment gateway", example = "1", requiredMode = RequiredMode.REQUIRED)
  @NotNull
  private Integer paymentGatewayId;

  @Schema(description = "Delivery address for the order")
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class Address {

    @Schema(description = "Street address including house number and street name", example = "123 Main Street", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String street;

    @Schema(description = "City name", example = "New York", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String city;

    @Schema(description = "Country code (ISO 3166-1 alpha-2)", example = "US", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String country; // Country code

    @Schema(description = "Postal or ZIP code", example = "10001", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String postalCode;
  }
}
