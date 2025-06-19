package org.atlas.tools.mockserver.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.atlas.tools.mockserver.model.EndpointDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class EndpointConfig {

  @Bean
  public List<EndpointDefinition> endpointDefinitions() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(
        new ClassPathResource("endpoints.json").getInputStream(),
        new TypeReference<>() {
        }
    );
  }
}
