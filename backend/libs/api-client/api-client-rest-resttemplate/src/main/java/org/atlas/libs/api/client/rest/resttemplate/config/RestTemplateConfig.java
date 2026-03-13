package org.atlas.libs.api.client.rest.resttemplate.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.atlas.libs.api.client.rest.resttemplate.context.RestClientUserContextInterceptor;
import org.atlas.libs.api.client.rest.resttemplate.logging.RestClientLoggingInterceptor;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

  private final ApiClientRestProps apiClientRestProps;

  /**
   * As the {@link RestClientLoggingInterceptor} consumes the response stream, our client
   * application will see an empty response body. To avoid that, we should use
   * BufferingClientHttpRequestFactory: it buffers stream content into memory. This way, it can be
   * read twice: once by our interceptor, and a second time by our client application.
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder)
      throws NoSuchAlgorithmException, KeyManagementException {
    // Configure the request factory based on SSL settings
    BufferingClientHttpRequestFactory requestFactory = createRequestFactory();

    // Build the RestTemplate with common configurations
    return builder.requestFactory(() -> requestFactory)
        .connectTimeout(Duration.ofSeconds(apiClientRestProps.getConnectTimeout()))
        .readTimeout(Duration.ofSeconds(apiClientRestProps.getReadTimeout()))
        .additionalInterceptors(new RestClientLoggingInterceptor())
        .additionalInterceptors(new RestClientUserContextInterceptor())
        .build();
  }

  /**
   * Creates the appropriate request factory based on SSL settings.
   */
  private BufferingClientHttpRequestFactory createRequestFactory()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    // Trust-all SSL client (DEV/TEST ONLY)
    if (apiClientRestProps.getSsl() == null || !apiClientRestProps.getSsl().getEnabled()) {
      return new BufferingClientHttpRequestFactory(
          new HttpComponentsClientHttpRequestFactory(createTrustAllClient()));
    }

    CloseableHttpClient httpClient = HttpClients.createDefault();

    return new BufferingClientHttpRequestFactory(
        new HttpComponentsClientHttpRequestFactory(httpClient));
  }

  private CloseableHttpClient createTrustAllClient()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    TrustStrategy trustAll = (X509Certificate[] chain, String authType) -> true;

    SSLContext sslContext = SSLContexts.custom()
        .loadTrustMaterial(null, trustAll)
        .build();

    TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
        .setSslContext(sslContext)
        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
        .build();

    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsStrategy(tlsStrategy)
            .build();

    return HttpClients.custom()
        .setConnectionManager(connectionManager)
        .build();
  }
}
