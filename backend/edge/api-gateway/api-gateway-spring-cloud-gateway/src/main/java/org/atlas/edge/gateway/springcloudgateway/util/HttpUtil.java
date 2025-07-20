package org.atlas.edge.gateway.springcloudgateway.util;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.constant.HttpConstant;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@UtilityClass
public class HttpUtil {

  public <T> Mono<Void> respond(ServerWebExchange exchange,
      ApiResponseWrapper<T> apiResponseWrapperBody,
      HttpStatus httpStatus) {
    ServerHttpResponse response = exchange.getResponse();

    // Set status code and content type
    response.setStatusCode(httpStatus);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    // ApiResponseWrapper body
    String responseBodyJson = JsonUtil.getInstance().toJson(apiResponseWrapperBody);
    byte[] responseBodyJsonBytes = responseBodyJson.getBytes(StandardCharsets.UTF_8);

    // Convert response body to DataBuffer
    DataBuffer dataBuffer = response.bufferFactory()
        .wrap(responseBodyJsonBytes);

    // Write and complete the response
    return response.writeWith(Mono.just(dataBuffer));
  }

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
