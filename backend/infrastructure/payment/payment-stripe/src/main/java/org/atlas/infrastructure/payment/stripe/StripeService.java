package org.atlas.infrastructure.payment.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.util.CurrencyUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "payment.stripe")
public class StripeService {

  private final StripeClient stripeClient;
  private final StripeProps stripeProps;

  public PaymentIntent createPaymentIntent(BigDecimal amount, String currency,
      Map<String, String> metadata)
      throws StripeException {
    PaymentIntentCreateParams params =
        PaymentIntentCreateParams.builder()
            .setAmount(CurrencyUtil.getAmountInSmallestUnit(amount, currency))
            .setCurrency(currency)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .putAllMetadata(metadata)
            .build();
    PaymentIntent paymentIntent = stripeClient.v1()
        .paymentIntents()
        .create(params);
    log.info("Created new PaymentIntent {} successfully: amount={}, currency={}",
        paymentIntent.getId(), amount, currency);
    return paymentIntent;
  }

  public boolean verifyWebhookSignature(String payload, String sigHeader) {
    try {
      // Use webhook endpoint secret from configuration
      String endpointSecret = stripeProps.getWebhookEndpointSecret();
      if (endpointSecret == null || endpointSecret.isEmpty()) {
        log.warn("Webhook endpoint secret not configured");
        return false;
      }

      Webhook.constructEvent(payload, sigHeader, endpointSecret);
      return true;
    } catch (SignatureVerificationException e) {
      log.warn("Failed to verify webhook signature: {}", e.getMessage());
      return false;
    }
  }
}
