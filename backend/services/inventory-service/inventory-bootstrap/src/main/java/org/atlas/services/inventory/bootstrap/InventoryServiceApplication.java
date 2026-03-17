package org.atlas.services.inventory.bootstrap;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class InventoryServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(InventoryServiceApplication.class)
        .initializers(new org.atlas.libs.yamlloader.YamlLoader())
        .run(args);
  }
}
