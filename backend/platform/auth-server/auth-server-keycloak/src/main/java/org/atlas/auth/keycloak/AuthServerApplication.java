package org.atlas.auth.keycloak;

import org.atlas.common.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class AuthServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthServerApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
