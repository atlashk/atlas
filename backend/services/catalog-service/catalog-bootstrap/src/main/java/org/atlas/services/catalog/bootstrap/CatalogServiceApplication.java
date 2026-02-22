package org.atlas.services.catalog.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class CatalogServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(CatalogServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
