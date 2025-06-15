package org.atlas.framework.util;

import lombok.experimental.UtilityClass;
import org.atlas.framework.cryptography.Base64Util;

@UtilityClass
public class ImageUtil {

  public static String toBase64(byte[] imageBytes) {
    return "data:image/jpeg;base64," + Base64Util.encode(imageBytes);
  }

  public static byte[] fromBase64(String base64St) {
    if (base64St.startsWith("data:image")) {
      base64St = base64St.substring(base64St.indexOf(",") + 1);
    }
    return Base64Util.decode(base64St);
  }
}
