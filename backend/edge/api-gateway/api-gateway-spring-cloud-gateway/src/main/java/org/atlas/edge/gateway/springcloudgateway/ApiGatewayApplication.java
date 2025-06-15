package org.atlas.edge.gateway.springcloudgateway;

import org.atlas.infrastructure.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {
    "org.atlas"
})
public class ApiGatewayApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ApiGatewayApplication.class)
        .initializers(new YamlConfigLoader()).run(args);
  }
}
