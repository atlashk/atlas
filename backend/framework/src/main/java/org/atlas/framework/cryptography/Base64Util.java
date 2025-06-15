package org.atlas.framework.cryptography;

import java.util.Base64;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Base64Util {

  public static String encode(byte[] input) {
    return Base64.getEncoder().encodeToString(input);
  }

  public static byte[] decode(String input) {
    return Base64.getDecoder().decode(input);
  }
}
