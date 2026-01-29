package org.atlas.platform.auth.keycloak;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class AuthServerKeycloakApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthServerKeycloakApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
