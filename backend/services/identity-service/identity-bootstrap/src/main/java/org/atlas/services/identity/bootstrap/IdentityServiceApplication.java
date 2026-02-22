package org.atlas.services.identity.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class IdentityServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(IdentityServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
