package org.atlas.infrastructure.api.client.rest.restclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  /**
   * Configures a RestClient with BufferingClientHttpRequestFactory to allow interceptors to read
   * the response body without consuming it.
   */
  @Bean
  public RestClient restClient() {
    return RestClient.builder()
        .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .requestInterceptor(new LoggingRequestInterceptor())
        .requestInterceptor(new UserContextRequestInterceptor())
        .build();
  }
}
