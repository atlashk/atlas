package org.atlas.libs.api.server.rest.serialization;

import org.atlas.libs.framework.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return JsonUtil.JSON_MAPPER;
  }
}
