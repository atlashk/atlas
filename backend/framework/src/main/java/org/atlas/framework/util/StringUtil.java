package org.atlas.framework.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

  public static final String EMPTY = "";

  /**
   * Checks if a string is blank (null, empty, or containing only whitespace). Using Java 11+
   * isBlank built-in method directly
   */
  public static boolean isBlank(String str) {
    return str == null || str.isBlank();
  }

  /**
   * Checks if a string is not blank (not null, not empty, and contains non-whitespace characters).
   *
   * @param str the string to check
   * @return true if the string is not null, not empty, and contains non-whitespace characters;
   * false otherwise
   */
  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  /**
   * Returns the input string if it is not null; otherwise, returns an empty value by default.
   */
  public static String nvl(String str) {
    return str == null ? StringUtil.EMPTY : str;
  }

  public static String nvl(Object str) {
    return str == null ? StringUtil.EMPTY : str.toString();
  }

  /**
   * Limits the length of a string to the specified maximum length. If the string exceeds the
   * maximum length, it is truncated.
   *
   */
  public static String limitLength(String str, int maxLength) {
    if (str == null) {
      return null;
    }
    return str.length() > maxLength ? str.substring(0, maxLength) : str;
  }

  /**
   * Masks a string by keeping a specified number of characters at the start and replacing the rest
   * with a mask character.
   */
  public static String mask(String str, int firstChars, char maskChar) {
    if (str == null) {
      return null;
    }

    int strLength = str.length();
    if (firstChars <= 0) {
      return String.valueOf(maskChar).repeat(strLength);
    }

    if (strLength <= firstChars) {
      return str;
    }

    return str.substring(0, firstChars) + String.valueOf(maskChar).repeat(strLength - firstChars);
  }

  /**
   * Randomly shuffles the characters of a string.
   */
  public static String shuffle(String str) {
    if (str == null) {
      return EMPTY;
    }

    List<Character> characters = new ArrayList<>();
    for (char character : str.toCharArray()) {
      characters.add(character);
    }
    Collections.shuffle(characters);

    StringBuilder shuffled = new StringBuilder();
    for (char character : characters) {
      shuffled.append(character);
    }
    return shuffled.toString();
  }

  public static String sanitizeErrorMessage(String errorMessage) {
    if (StringUtil.isBlank(errorMessage)) {
      return "Unknown error";
    }

    // Limit length to prevent database issues
    if (errorMessage.length() > 1000) {
      return errorMessage.substring(0, 997) + "...";
    }

    return errorMessage;
  }
}
