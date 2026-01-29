package org.atlas.libs.payment.stripe;

import com.stripe.StripeClient;
import org.atlas.libs.framework.payment.exception.PaymentGatewayException;
import org.atlas.libs.framework.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

  @Bean
  public StripeClient stripeClient(StripeProps stripeProps) {
    // Verify required properties
    if (StringUtil.isBlank(stripeProps.getPublishableKey())) {
      throw new PaymentGatewayException("Stripe publishable key is required");
    }
    if (StringUtil.isBlank(stripeProps.getSecretKey())) {
      throw new PaymentGatewayException("Stripe secret key is required");
    }
    if (StringUtil.isBlank(stripeProps.getWebhookEndpointSecret())) {
      throw new PaymentGatewayException("Stripe webhook endpoint secret is required");
    }

    return StripeClient.builder()
        .setApiKey(stripeProps.getSecretKey())
        .setConnectTimeout(30 * 1000) // in milliseconds
        .setReadTimeout(80 * 1000) // in milliseconds
        .build();
  }
}
