package org.atlas.infrastructure.payment.stripe;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.payment.stripe")
@Getter
@Setter
public class StripeProps {

  private String secretKey;
  private String publishableKey;
  private String webhookEndpointSecret;
}
