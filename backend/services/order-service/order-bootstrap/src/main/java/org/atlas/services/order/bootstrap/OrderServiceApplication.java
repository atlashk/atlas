package org.atlas.services.order.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class OrderServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(OrderServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
