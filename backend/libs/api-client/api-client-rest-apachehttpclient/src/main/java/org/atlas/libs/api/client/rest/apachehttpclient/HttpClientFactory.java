package org.atlas.libs.api.client.rest.apachehttpclient;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import org.atlas.libs.framework.api.client.rest.SSLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpClientFactory {

  @Value("${app.api-client.rest.ssl.enabled:false}")
  private boolean sslEnabled;

  /**
   * Creates a custom HttpClient with logging and user context interceptors. If SSL is disabled, it
   * will skip SSL certificate validation.
   *
   * @return A configured HttpClient instance
   */
  public CloseableHttpClient custom() {
    try {
      return sslEnabled ? createDefaultClient() : createTrustAllClient();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create HttpClient", e);
    }
  }

  /**
   * Creates a default HttpClient with standard SSL validation.
   *
   * @return A configured HttpClient instance
   */
  private CloseableHttpClient createDefaultClient() {
    return applyInterceptors(HttpClients.custom()).build();
  }

  /**
   * Creates an HttpClient that trusts all SSL certificates. This should only be used when SSL
   * validation is explicitly disabled.
   *
   * @return A configured HttpClient instance that skips SSL validation
   * @throws NoSuchAlgorithmException if the SSL context algorithm is not available
   * @throws KeyStoreException        if there is an error with the keystore
   * @throws KeyManagementException   if there is an error with key management
   */
  private CloseableHttpClient createTrustAllClient()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    // Create an SSL context that trusts all certificates
    SSLContext sslContext = SSLUtil.createTrustAllSSLContext();

    // Create a TLS strategy with the trust-all SSL context
    DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext,
        NoopHostnameVerifier.INSTANCE);

    // Build a connection manager with the custom TLS strategy
    return applyInterceptors(HttpClients.custom()
        .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(Timeout.ofMinutes(1))
                .build())
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setConnPoolPolicy(PoolReusePolicy.LIFO)
            .build()))
        .build();
  }

  /**
   * Applies common interceptors to the HttpClient builder.
   *
   * @param builder The HttpClient builder to configure
   * @return The configured HttpClient builder
   */
  private HttpClientBuilder applyInterceptors(HttpClientBuilder builder) {
    return builder
        .addRequestInterceptorFirst(new LoggingRequestInterceptor())
        .addRequestInterceptorFirst(new UserContextRequestInterceptor())
        .addResponseInterceptorFirst(new LoggingResponseInterceptor());
  }
}
