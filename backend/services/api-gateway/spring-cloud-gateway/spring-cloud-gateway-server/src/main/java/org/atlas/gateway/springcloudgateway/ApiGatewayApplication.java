package org.atlas.gateway.springcloudgateway;

import org.atlas.common.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class ApiGatewayApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ApiGatewayApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
