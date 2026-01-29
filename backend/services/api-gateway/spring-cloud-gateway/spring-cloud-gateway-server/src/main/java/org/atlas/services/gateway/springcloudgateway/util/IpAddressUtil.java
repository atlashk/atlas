package org.atlas.services.gateway.springcloudgateway.util;

import java.net.InetSocketAddress;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.http.HttpConstant;
import org.atlas.libs.framework.util.StringUtil;
import org.springframework.http.server.reactive.ServerHttpRequest;

@UtilityClass
public class IpAddressUtil {

  /**
   * Extracts the client IP address from the request, prioritizing proxy headers.
   */
  public static String getIpAddress(ServerHttpRequest request) {
    if (request == null) {
      return "unknown";
    }

    // Check proxy headers
    for (String header : HttpConstant.IP_ADDRESS_HEADERS) {
      String ipAddress = request.getHeaders().getFirst(header);
      if (StringUtil.isNotBlank(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress)) {
        return ipAddress.split(",")[0].trim();
      }
    }

    // Fallback to remote address
    InetSocketAddress remoteAddress = request.getRemoteAddress();
    if (remoteAddress != null && remoteAddress.getAddress() != null) {
      return remoteAddress.getAddress().getHostAddress();
    }

    return "unknown";
  }
}
