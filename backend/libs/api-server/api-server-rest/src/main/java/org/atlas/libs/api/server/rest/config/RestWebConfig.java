package org.atlas.libs.api.server.rest.config;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.api.server.rest.converter.FileTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class RestWebConfig implements WebMvcConfigurer {

  private final FileTypeConverter fileTypeConverter;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(fileTypeConverter);
  }

  /**
   * Set the default media type for the responses
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.defaultContentType(MediaType.APPLICATION_JSON);
  }
}
