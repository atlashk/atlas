package org.atlas.services.product.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class ProductServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ProductServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
