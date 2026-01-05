package org.atlas.discovery.eureka;

import org.atlas.common.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication(scanBasePackages = "org.atlas")
@EnableEurekaServer
public class DiscoveryServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DiscoveryServerApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
