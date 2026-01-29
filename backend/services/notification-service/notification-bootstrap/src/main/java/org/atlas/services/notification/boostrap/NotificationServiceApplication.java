package org.atlas.services.notification.boostrap;

import org.atlas.libs.framework.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.atlas")
public class NotificationServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(NotificationServiceApplication.class)
        .initializers(new YamlConfigLoader())
        .run(args);
  }
}
