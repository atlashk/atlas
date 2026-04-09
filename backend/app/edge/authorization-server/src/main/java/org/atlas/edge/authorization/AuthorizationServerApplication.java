package org.atlas.edge.authorization;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class AuthorizationServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthorizationServerApplication.class)
        .initializers(new org.atlas.libs.yamlloader.YamlLoader())
        .run(args);
  }
}
