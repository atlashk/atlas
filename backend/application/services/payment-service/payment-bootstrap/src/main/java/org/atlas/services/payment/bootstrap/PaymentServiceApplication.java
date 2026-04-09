package org.atlas.services.payment.bootstrap;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class PaymentServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(PaymentServiceApplication.class)
        .initializers(new org.atlas.libs.yamlloader.YamlLoader()).run(args);
  }
}
