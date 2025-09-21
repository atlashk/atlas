package org.atlas.domain.order.usecase.front.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.atlas.domain.payment.shared.PaymentMethod;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontPlaceOrderInput {

  @NotEmpty
  private List<@Valid OrderItem> orderItems;

  @NotNull
  private PaymentMethod paymentMethod;

  @Getter
@Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItem {

    @NotNull
    private Integer productId;

    @NotNull
    @Min(1)
    private Integer quantity;
  }
}
