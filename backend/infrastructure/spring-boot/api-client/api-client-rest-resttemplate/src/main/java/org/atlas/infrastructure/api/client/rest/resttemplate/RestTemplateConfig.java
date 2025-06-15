package org.atlas.infrastructure.api.client.rest.resttemplate;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  /**
   * As the {@link LoggingRequestInterceptor} consumes the response stream, our client application
   * will see an empty response body. To avoid that, we should use
   * BufferingClientHttpRequestFactory: it buffers stream content into memory. This way, it can be
   * read twice: once by our interceptor, and a second time by our client application.
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.requestFactory(
            () -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .additionalInterceptors(new LoggingRequestInterceptor())
        .additionalInterceptors(new UserContextRequestInterceptor())
        .build();
  }
}
