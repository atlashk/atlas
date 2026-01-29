package org.atlas.services.user.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class UserServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(UserServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
