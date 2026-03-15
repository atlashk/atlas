package org.atlas.platform.authorization.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class AuthorizationServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthorizationServerApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
