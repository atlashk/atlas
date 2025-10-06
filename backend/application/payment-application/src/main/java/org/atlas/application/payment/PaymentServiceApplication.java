package org.atlas.application.payment;

import org.atlas.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class PaymentServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(PaymentServiceApplication.class)
        .initializers(new YamlConfigLoader()).run(args);
  }
}
