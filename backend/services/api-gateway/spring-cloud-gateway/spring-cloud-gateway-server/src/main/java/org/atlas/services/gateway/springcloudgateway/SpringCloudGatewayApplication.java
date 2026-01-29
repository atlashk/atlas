package org.atlas.services.gateway.springcloudgateway;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class SpringCloudGatewayApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(SpringCloudGatewayApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
