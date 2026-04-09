package org.atlas.edge.gateway.reactive;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class ReactiveGatewayApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ReactiveGatewayApplication.class)
        .initializers(new org.atlas.libs.yamlloader.YamlLoader())
        .run(args);
  }
}
