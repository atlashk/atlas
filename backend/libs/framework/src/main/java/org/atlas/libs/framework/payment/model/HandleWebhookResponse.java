package org.atlas.libs.framework.payment.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.payment.PaymentStatus;
import org.atlas.libs.framework.payment.method.PaymentMethodDetails;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class HandleWebhookResponse {

  public static final String BODY_FIELD_ERROR = "error";

  private Result result;

  // Respond to the external payment gateway
  private int responseStatus;
  private Map<String, Object> responseBody;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Result {

    private String paymentId;
    private String paymentMethod;
    private PaymentMethodDetails paymentMethodDetails;
    private PaymentStatus status;
    private String error;
    private String cancellationReason;
  }
}
