package org.atlas.edge.auth.springsecurityjwt;

import org.atlas.infrastructure.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {
    "org.atlas"
})
public class AuthServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthServerApplication.class)
        .initializers(new YamlConfigLoader()).run(args);
  }
}
