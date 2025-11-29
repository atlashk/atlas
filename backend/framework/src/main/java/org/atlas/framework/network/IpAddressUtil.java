package org.atlas.framework.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class IpAddressUtil {

  public static List<String> getLocalHostIps() {
    List<String> localHostIps = new ArrayList<>();
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface netInterface = interfaces.nextElement();
        if (!netInterface.isUp() || netInterface.isLoopback() || netInterface.isVirtual()) {
          continue;
        }

        Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress ip = inetAddresses.nextElement();
          if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
            localHostIps.add(ip.getHostAddress());
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to get local host IPs", e);
    }
    return localHostIps;
  }
}
