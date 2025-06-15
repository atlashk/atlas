package org.atlas.framework.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HttpConstant {

  public static final String[] IP_ADDRESS_HEADERS = {
      "x-forwarded-for",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "X-Real-IP"
  };
}
