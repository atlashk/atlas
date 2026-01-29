package org.atlas.platform.discovery.eureka;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication(scanBasePackages = "org.atlas")
@EnableEurekaServer
public class EurekaServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(EurekaServerApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
