package org.atlas.framework.cryptography;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;

@UtilityClass
public class Sha256Util {

  public static String encode(String input) {
    return DigestUtils.sha256Hex(input);
  }
}
