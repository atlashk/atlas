package org.atlas.framework.i18n;

import java.util.Locale;

public interface I18nPort {

  String getMessage(String code, Object... params);

  String getMessage(String code, Locale locale, Object... params);

  String getMessage(String code, String defaultValue, Object... params);

  String getMessage(String code, String defaultValue, Locale locale, Object... params);
}
