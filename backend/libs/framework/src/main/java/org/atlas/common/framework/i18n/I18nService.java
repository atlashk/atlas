package org.atlas.common.framework.i18n;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.atlas.common.framework.util.StringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class I18nService {

  private final MessageSource messageSource;
  private final Locale locale;

  public String getMessage(String code, Object... params) {
    return getMessage(code, StringUtil.EMPTY, locale, params);
  }

  public String getMessage(String code, Locale locale, Object... params) {
    return getMessage(code, StringUtil.EMPTY, locale, params);
  }

  public String getMessage(String code, String defaultValue, Object... params) {
    return getMessage(code, defaultValue, locale, params);
  }

  public String getMessage(String code, String defaultValue, Locale locale, Object... params) {
    try {
      return messageSource.getMessage(code, params, locale);
    } catch (NoSuchMessageException e) {
      return defaultValue;
    }
  }
}
