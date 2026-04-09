package org.atlas.libs.api.server.grpc.util;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import java.net.SocketAddress;
import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.http.HttpConstant;
import org.atlas.libs.framework.util.StringUtil;

@UtilityClass
public class GrpcIpAddressUtil {

  public static String getIpAddress(ServerCall<?, ?> serverCall, Metadata metadata) {
    for (String headerName : HttpConstant.IP_ADDRESS_HEADERS) {
      Metadata.Key<String> key = Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER);
      String ipAddress = metadata.get(key);
      if (StringUtil.isNotBlank(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress)) {
        return ipAddress.split(",")[0].trim();
      }
    }

    SocketAddress remoteAddr = serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
    if (remoteAddr != null) {
      String remoteAddrStr = remoteAddr.toString();
      if (remoteAddrStr.startsWith("/")) {
        remoteAddrStr = remoteAddrStr.substring(1);
      }
      if (remoteAddrStr.contains(":")) {
        remoteAddrStr = remoteAddrStr.split(":")[0];
      }
      return remoteAddrStr;
    }

    return "unknown";
  }
}
