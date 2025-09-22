package org.atlas.infrastructure.api.server.rest.core.config;

import lombok.RequiredArgsConstructor;
import org.atlas.infrastructure.api.server.rest.core.converter.FileTypeConverter;
import org.atlas.infrastructure.api.server.rest.core.converter.PaymentMethodConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class RestWebConfig implements WebMvcConfigurer {

  private final FileTypeConverter fileTypeConverter;
  private final PaymentMethodConverter paymentMethodConverter;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(fileTypeConverter);
    registry.addConverter(paymentMethodConverter);
  }

  /**
   * Set the default media type for the responses
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.defaultContentType(MediaType.APPLICATION_JSON);
  }
}
