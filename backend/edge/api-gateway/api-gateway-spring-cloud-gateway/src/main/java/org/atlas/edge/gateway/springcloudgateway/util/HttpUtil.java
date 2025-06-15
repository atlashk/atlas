package org.atlas.edge.gateway.springcloudgateway.util;

import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.json.JsonUtil;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
}
