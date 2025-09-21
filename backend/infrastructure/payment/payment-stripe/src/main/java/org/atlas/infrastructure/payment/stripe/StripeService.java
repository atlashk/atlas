package org.atlas.infrastructure.payment.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.ChargeCreateParams;
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
      StripePaymentMethod paymentMethod, Map<String, String> metadata)
      throws StripeException {
    PaymentIntentCreateParams params =
        PaymentIntentCreateParams.builder()
            .setAmount(CurrencyUtil.getAmountInSmallestUnit(amount, currency))
            .setCurrency(currency)
            .setPaymentMethod(paymentMethod.getType())
            .putAllMetadata(metadata)
            .build();
    PaymentIntent paymentIntent = stripeClient.v1()
        .paymentIntents()
        .create(params);
    log.info("Created new PaymentIntent {} successfully: amount={}, currency={}, paymentMethod={}",
        paymentIntent.getId(), amount, currency, paymentMethod);
    return paymentIntent;
  }

  public Charge createCharge(BigDecimal amount, String currency, String cardToken)
      throws StripeException {
    ChargeCreateParams params = ChargeCreateParams.builder()
        .setSource(cardToken)
        .setAmount(CurrencyUtil.getAmountInSmallestUnit(amount, currency))
        .setCurrency(currency)
        .build();
    Charge charge = stripeClient.v1()
        .charges()
        .create(params);
    log.info("Created new Charge {} successfully: amount={}, currency={}",
        charge.getId(), amount, currency);
    return charge;
  }

  public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
    PaymentIntent paymentIntent = stripeClient.v1()
        .paymentIntents()
        .retrieve(paymentIntentId);
    log.debug("Retrieved PaymentIntent {} successfully: status={}",
        paymentIntent.getId(), paymentIntent.getStatus());
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
