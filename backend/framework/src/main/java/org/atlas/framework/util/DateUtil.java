package org.atlas.framework.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class DateUtil {

  private static final ConcurrentMap<String, SimpleDateFormat> simpleDateFormatCache = new ConcurrentHashMap<>();

  public static Date parse(String source, String pattern) {
    if (StringUtils.isBlank(source)) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = simpleDateFormatCache.computeIfAbsent(pattern,
        SimpleDateFormat::new);
    try {
      return simpleDateFormat.parse(source);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static String format(Date source, String pattern) {
    if (source == null) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = simpleDateFormatCache.computeIfAbsent(pattern,
        SimpleDateFormat::new);
    return simpleDateFormat.format(source);
  }

  public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
    return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault())
        .toInstant()) : null;
  }

  public static LocalDateTime convertDateToLocalDateTime(Date date) {
    return date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
  }

  public static Date now() {
    return new Date();
  }

  public static String now(String pattern) {
    return format(now(), pattern);
  }

  public static Date today() {
    Calendar calendar = Calendar.getInstance();
    resetToMidnight(calendar);
    return calendar.getTime();
  }

  public static Date tomorrow() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    resetToMidnight(calendar);
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    return calendar.getTime();
  }

  public static Date yesterday() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    resetToMidnight(calendar);
    calendar.add(Calendar.DAY_OF_YEAR, -1);
    return calendar.getTime();
  }

  public static LocalDateTime changeTimezone(LocalDateTime source, int offsetInHours) {
    ZonedDateTime sourceZonedDateTime = source.atZone(ZoneId.systemDefault());
    ZoneOffset targetZoneOffset = ZoneOffset.ofHours(offsetInHours);
    ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(targetZoneOffset);
    return targetZonedDateTime.toLocalDateTime();
  }

  public static Date changeTimezone(Date source, int offsetInHours) {
    Instant sourceInstant = source.toInstant();
    ZonedDateTime sourceZonedDateTime = sourceInstant.atZone(ZoneId.systemDefault());
    ZoneOffset targetZoneOffset = ZoneOffset.ofHours(offsetInHours);
    ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(targetZoneOffset);
    LocalDateTime targetLocalDateTime = targetZonedDateTime.toLocalDateTime();
    return convertLocalDateTimeToDate(targetLocalDateTime);
  }

  private static void resetToMidnight(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }
}
