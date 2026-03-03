package org.atlas.libs.api.server.rest.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atlas.libs.framework.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return JsonUtil.OBJECT_MAPPER;
  }
}
