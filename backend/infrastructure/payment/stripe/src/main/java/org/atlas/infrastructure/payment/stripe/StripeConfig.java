package org.atlas.infrastructure.payment.stripe;

import com.stripe.StripeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

  @Bean
  public StripeClient stripeClient(StripeProps stripeProps) {
    return StripeClient.builder()
        .setApiKey(stripeProps.getSecretKey())
        .setConnectTimeout(30 * 1000) // in milliseconds
        .setReadTimeout(80 * 1000) // in milliseconds
        .build();
  }
}
