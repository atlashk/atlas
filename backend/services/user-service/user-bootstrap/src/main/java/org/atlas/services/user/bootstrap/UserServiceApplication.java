package org.atlas.services.user.bootstrap;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class UserServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(UserServiceApplication.class)
        .initializers(new org.atlas.libs.yamlloader.YamlLoader())
        .run(args);
  }
}
