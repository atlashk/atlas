package org.atlas.platform.config;

import org.atlas.libs.yamlloader.YamlLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication(scanBasePackages = "org.atlas")
@EnableConfigServer
public class ConfigServerApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ConfigServerApplication.class)
        .initializers(new YamlLoader())
        .run(args);
  }
}
