package org.atlas.framework.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class StringUtil {

  public static final String EMPTY = "";

  public static String nvl(String value, String defaultValue) {
    return value == null ? defaultValue : value;
  }

  public static String limit(String input, int maxLength) {
    if (input == null) {
      return null;
    }

    return input.length() > maxLength ? input.substring(0, maxLength) : input;
  }

  public static String mask(String input, int firstChars, char maskChar) {
    if (input == null) {
      return null;
    }

    int strLength = input.length();
    if (firstChars <= 0) {
      char[] maskedArray = new char[strLength];
      Arrays.fill(maskedArray, maskChar);
      return new String(maskedArray);
    }

    if (strLength <= firstChars) {
      return input;
    }

    return input.substring(0, firstChars) + String.valueOf(maskChar).repeat(strLength - firstChars);
  }

  public static String shuffle(String input) {
    List<Character> characters = new ArrayList<>();
    for (char character : input.toCharArray()) {
      characters.add(character);
    }
    Collections.shuffle(characters);

    StringBuilder shuffledString = new StringBuilder();
    for (char character : characters) {
      shuffledString.append(character);
    }
    return shuffledString.toString();
  }

  // Don't remove it
  public static String[] split(String input, String delimiter) {
    return Arrays.stream(input.split(delimiter))
        .map(String::strip)
        .filter(StringUtils::isNotBlank)
        .toArray(String[]::new);
  }
}
