package org.atlas.libs.framework.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.experimental.UtilityClass;

/**
 * Utility class for modern java.time.* operations.
 */
@UtilityClass
public class DateUtil {

  private static final ConcurrentMap<String, DateTimeFormatter> dateTimeFormatterCache =
      new ConcurrentHashMap<>();

  public static LocalDateTime parse(String source, String pattern) {
    if (StringUtil.isBlank(source)) {
      return null;
    }

    DateTimeFormatter dateTimeFormatter = dateTimeFormatterCache.computeIfAbsent(pattern,
        DateTimeFormatter::ofPattern);
    return LocalDateTime.parse(source, dateTimeFormatter);
  }

  public static String format(LocalDateTime source, String pattern) {
    if (source == null) {
      return null;
    }

    DateTimeFormatter dateTimeFormatter = dateTimeFormatterCache.computeIfAbsent(pattern,
        DateTimeFormatter::ofPattern);
    return dateTimeFormatter.format(source);
  }

  public static LocalDateTime getMidnight(LocalDate date) {
    if (date == null) {
      return null;
    }

    return date.atStartOfDay();
  }

  public static LocalDateTime getNextMidnight(LocalDate date) {
    if (date == null) {
      return null;
    }

    return date.plusDays(1).atStartOfDay();
  }
}
