package org.atlas.common.infrastructure.api.server.rest.core.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atlas.common.framework.json.jackson.JacksonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return JacksonService.OBJECT_MAPPER;
  }
}
