package org.atlas.infrastructure.api.server.rest.core.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atlas.framework.json.jackson.JacksonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return JacksonService.objectMapper;
  }
}
