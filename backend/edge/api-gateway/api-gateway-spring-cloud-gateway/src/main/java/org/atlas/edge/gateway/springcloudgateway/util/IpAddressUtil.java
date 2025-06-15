package org.atlas.edge.gateway.springcloudgateway.util;

import java.net.InetSocketAddress;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.atlas.framework.constant.HttpConstant;
import org.springframework.http.server.reactive.ServerHttpRequest;

@UtilityClass
public class IpAddressUtil {

  /**
   * Extracts the client IP address from the request, prioritizing proxy headers.
   *
   * @param request the ServerHttpRequest
   * @return the client IP address or "unknown" if not found
   */
  public static String getIpAddress(ServerHttpRequest request) {
    if (request == null) {
      return "unknown";
    }

    // Check proxy headers
    for (String header : HttpConstant.IP_ADDRESS_HEADERS) {
      String ipAddress = request.getHeaders().getFirst(header);
      if (isValidIpAddress(ipAddress)) {
        return extractFirstIpAddress(ipAddress);
      }
    }

    // Fallback to remote address
    InetSocketAddress remoteAddress = request.getRemoteAddress();
    if (remoteAddress != null && remoteAddress.getAddress() != null) {
      return remoteAddress.getAddress().getHostAddress();
    }

    return "unknown";
  }

  /**
   * Checks if the IP address string is valid (non-empty and not "unknown").
   *
   * @param ipAddress the IP address string
   * @return true if valid, false otherwise
   */
  private static boolean isValidIpAddress(String ipAddress) {
    return StringUtils.isNotBlank(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress);
  }

  /**
   * Extracts the first IP address from a comma-separated list (e.g., X-Forwarded-For).
   *
   * @param ipAddress the IP address string
   * @return the first IP address or the original string if not a list
   */
  private static String extractFirstIpAddress(String ipAddress) {
    if (ipAddress != null && ipAddress.contains(",")) {
      return ipAddress.split(",")[0].trim();
    }
    return ipAddress;
  }
}
