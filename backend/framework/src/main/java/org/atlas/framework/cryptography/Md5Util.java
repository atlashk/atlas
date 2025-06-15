package org.atlas.framework.cryptography;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;

@UtilityClass
public class Md5Util {

  public static String encode(String input) {
    return DigestUtils.md5Hex(input);
  }
}
