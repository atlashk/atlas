package org.atlas.infrastructure.api.client.rest.apachehttpclient;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class HttpClientFactory {

  public static CloseableHttpClient custom() {
    return HttpClients.custom()
        .addRequestInterceptorFirst(new LoggingRequestInterceptor())
        .addRequestInterceptorFirst(new UserContextRequestInterceptor())
        .addResponseInterceptorFirst(new LoggingResponseInterceptor())
        .build();
  }
}
