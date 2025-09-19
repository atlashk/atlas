package org.atlas.framework.cryptography;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;

@UtilityClass
public class HashingUtil {

  public static String md5ToHex(String input) {
    return DigestUtils.md5Hex(input);
  }

  public static String sha256ToHex(String input) {
    return DigestUtils.sha256Hex(input);
  }
}
