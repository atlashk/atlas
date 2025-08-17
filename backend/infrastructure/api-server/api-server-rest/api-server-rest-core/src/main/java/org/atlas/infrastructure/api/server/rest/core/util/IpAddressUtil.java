package org.atlas.infrastructure.api.server.rest.core.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.atlas.framework.constant.HttpConstant;
import org.springframework.util.StringUtils;

@UtilityClass
public class IpAddressUtil {

  /**
   * Extracts the client IP address from the request, prioritizing proxy headers.
   *
   * @param request the HttpServletRequest
   * @return the client IP address or "unknown" if not found
   */
  public static String getIpAddress(HttpServletRequest request) {
    if (request == null) {
      return "unknown";
    }

    // Check proxy headers
    for (String header : HttpConstant.IP_ADDRESS_HEADERS) {
      String ipAddress = request.getHeader(header);
      if (isValidIpAddress(ipAddress)) {
        return extractFirstIpAddress(ipAddress);
      }
    }

    // Fallback to remote address
    String remoteAddr = request.getRemoteAddr();
    if (remoteAddr != null) {
      return remoteAddr;
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
    return StringUtils.hasText(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress);
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
