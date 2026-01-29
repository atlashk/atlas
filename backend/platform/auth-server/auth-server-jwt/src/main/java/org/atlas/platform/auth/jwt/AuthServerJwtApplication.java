package org.atlas.platform.auth.jwt;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class AuthServerJwtApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthServerJwtApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
