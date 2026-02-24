package org.atlas.services.inventory.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class InventoryServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(InventoryServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
