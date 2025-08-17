package org.atlas.infrastructure.api.client.rest.apachehttpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.util.StringUtils;

@Slf4j
public class LoggingRequestInterceptor implements HttpRequestInterceptor {

  private static final int MAX_PAYLOAD_SIZE = 1024 * 1024; // 1 MB in bytes

  @Override
  public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext)
      throws HttpException, IOException {
    String method = httpRequest.getMethod();
    String url = httpRequest.getRequestUri();
    String headers = getHeaders(httpRequest);
    String body = getBody(httpRequest);

    if (StringUtils.hasLength(body)) {
      log.info("Request: Method = {}, URL = {}, Headers = {}, Request body = {}", method, url,
          headers, body);
    } else {
      log.info("Request: Method = {}, URL = {}, Headers = {}", method, url, headers);
    }
  }

  private String getHeaders(HttpRequest httpRequest) {
    return "{" + Arrays.stream(httpRequest.getHeaders())
        .map(header -> header.getName() + "=" + header.getValue())
        .collect(Collectors.joining(", ")) + "}";
  }

  private String getBody(HttpRequest httpRequest) throws IOException {
    if (httpRequest instanceof BasicClassicHttpRequest) {
      HttpEntity httpEntity = ((BasicClassicHttpRequest) httpRequest).getEntity();
      if (httpEntity == null) {
        return null;
      }
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      httpEntity.writeTo(outputStream);
      byte[] content = outputStream.toByteArray();
      return new String(content, 0, Math.min(content.length, MAX_PAYLOAD_SIZE));
    }
    return null;
  }
}
