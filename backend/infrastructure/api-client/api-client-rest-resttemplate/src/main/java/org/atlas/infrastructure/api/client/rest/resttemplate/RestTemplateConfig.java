package org.atlas.infrastructure.api.client.rest.resttemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.atlas.framework.util.SSLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Value("${app.api-client.rest.ssl.enabled:false}")
  private boolean sslEnabled;

  /**
   * As the {@link LoggingRequestInterceptor} consumes the response stream, our client application
   * will see an empty response body. To avoid that, we should use
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
        .additionalInterceptors(new LoggingRequestInterceptor())
        .additionalInterceptors(new UserContextRequestInterceptor())
        .build();
  }

  /**
   * Creates the appropriate request factory based on SSL settings.
   */
  private BufferingClientHttpRequestFactory createRequestFactory()
      throws NoSuchAlgorithmException, KeyManagementException {
    if (!sslEnabled) { // Note: Only for dev/test environments - security risk
      return new BufferingClientHttpRequestFactory(createTrustAllRequestFactory());
    } else {
      return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }
  }

  /**
   * Creates a request factory that trusts all SSL certificates. This should only be used when SSL
   * validation is explicitly disabled.
   */
  private HttpComponentsClientHttpRequestFactory createTrustAllRequestFactory()
      throws NoSuchAlgorithmException, KeyManagementException {
    // Create an SSL context that trusts all certificates
    SSLContext sslContext = SSLUtil.createTrustAllSslContext();

    // Create a TLS strategy with the trust-all SSL context
    DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext,
        NoopHostnameVerifier.INSTANCE);

    // Build a connection manager with the custom TLS strategy
    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .build())
        .build();

    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);

    return requestFactory;
  }
}
