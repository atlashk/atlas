package org.atlas.libs.api.client.rest.restclient.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.util.Timeout;
import org.atlas.libs.api.client.rest.restclient.context.RestClientUserContextInterceptor;
import org.atlas.libs.api.client.rest.restclient.logging.RestClientLoggingInterceptor;
import org.atlas.libs.framework.api.rest.ApiClientRestProps;
import org.atlas.libs.framework.api.rest.SSLUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  private final ApiClientRestProps apiClientRestProps;

  /**
   * Configures a RestClient with BufferingClientHttpRequestFactory to allow interceptors to read
   * the response body without consuming it. SSL validation can be disabled via properties.
   */
  @Bean
  public RestClient restClient() {
    try {
      return RestClient.builder()
          .requestFactory(createRequestFactory())
          .requestInterceptor(new RestClientLoggingInterceptor())
          .requestInterceptor(new RestClientUserContextInterceptor())
          .build();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException("Failed to create request factory", e);
    }
  }

  private BufferingClientHttpRequestFactory createRequestFactory()
      throws NoSuchAlgorithmException, KeyManagementException {
    if (apiClientRestProps.isSslDisabled()) {
      return new BufferingClientHttpRequestFactory(createNoSslValidationRequestFactory());
    }
    return new BufferingClientHttpRequestFactory(createSimpleRequestFactory());
  }

  private SimpleClientHttpRequestFactory createSimpleRequestFactory() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(apiClientRestProps.getConnectTimeoutMillis());
    requestFactory.setReadTimeout(apiClientRestProps.getReadTimeoutMillis());
    return requestFactory;
  }

  private HttpComponentsClientHttpRequestFactory createNoSslValidationRequestFactory()
      throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLUtil.createTrustAllSSLContext();

    DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext,
        NoopHostnameVerifier.INSTANCE);

    ConnectionConfig connectionConfig = ConnectionConfig.custom()
        .setConnectTimeout(Timeout.ofSeconds(apiClientRestProps.getConnectTimeout()))
        .build();

    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .setDefaultConnectionConfig(connectionConfig)
            .build();

    RequestConfig requestConfig = RequestConfig.custom()
        .setResponseTimeout(Timeout.ofSeconds(apiClientRestProps.getReadTimeout()))
        .build();

    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig)
        .build();

    return new HttpComponentsClientHttpRequestFactory(httpClient);
  }
}
