package org.atlas.platform.config.spring;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication(scanBasePackages = "org.atlas")
@EnableConfigServer
public class SpringCloudConfigApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(SpringCloudConfigApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
