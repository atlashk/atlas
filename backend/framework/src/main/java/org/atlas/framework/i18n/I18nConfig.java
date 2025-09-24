package org.atlas.framework.i18n;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class I18nConfig {

  @Value("${app.locale:en-US}")
  private String defaultLocaleName;

  @Bean
  public MessageSource messageSource(Locale locale) {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    // src/main/resources/messages_{locale}.properties file
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setDefaultLocale(locale);
    return messageSource;
  }

  @Bean
  public Locale locale() {
    return Locale.forLanguageTag(defaultLocaleName);
  }
}
