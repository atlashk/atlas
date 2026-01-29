package org.atlas.libs.framework.random;

import java.security.SecureRandom;
import java.util.Base64;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.util.StringUtil;

@UtilityClass
public class RandomUtil {

  private static final SecureRandom secureRandom = new SecureRandom(); // thread-safe
  private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL_CHARS = "!@#$%^&*()-_+=<>?/{}~|";

  public static String randomPassword(int length, boolean useUppercase, boolean useDigits,
      boolean useSpecialChars) {
    StringBuilder password = new StringBuilder(length);
    String charCategories = LOWERCASE;
    if (useUppercase) {
      charCategories += UPPERCASE;
    }
    if (useDigits) {
      charCategories += DIGITS;
    }
    if (useSpecialChars) {
      charCategories += SPECIAL_CHARS;
    }
    for (int i = 0; i < length; i++) {
      password.append(charCategories.charAt(secureRandom.nextInt(charCategories.length())));
    }
    return StringUtil.shuffle(password.toString());
  }

  public static String randomOneTimeToken(int byteLength) {
    byte[] randomBytes = new byte[byteLength];
    secureRandom.nextBytes(randomBytes);
    return base64Encoder.encodeToString(randomBytes);
  }

  public static int randomInt(int minVal, int maxVal) {
    if (minVal < 0 || maxVal < 0 || minVal > maxVal) {
      throw new IllegalArgumentException("Invalid range: min=" + minVal + ", max=" + maxVal);
    }
    return minVal + secureRandom.nextInt(maxVal - minVal + 1);
  }
}
