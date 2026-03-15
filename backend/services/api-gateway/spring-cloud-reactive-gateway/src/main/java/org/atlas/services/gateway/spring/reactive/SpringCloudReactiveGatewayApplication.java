package org.atlas.services.gateway.spring.reactive;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class SpringCloudReactiveGatewayApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(SpringCloudReactiveGatewayApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
