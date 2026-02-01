package org.atlas.services.iam.bootstrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class IamServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(IamServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
