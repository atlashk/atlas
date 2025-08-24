package org.atlas.application.user;

import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.domain.service.DomainService;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.infrastructure.bootstrap.YamlConfigLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = {
    "org.atlas",
}, includeFilters = {
    @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {DomainService.class,
        UseCaseHandler.class,
        DomainEventHandler.class})
})
public class UserServiceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(UserServiceApplication.class)
        .initializers(new YamlConfigLoader()).run(args);
  }
}
