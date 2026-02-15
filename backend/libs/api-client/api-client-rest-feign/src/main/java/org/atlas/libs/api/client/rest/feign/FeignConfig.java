package org.atlas.libs.api.client.rest.feign;

import feign.Client;
import feign.Logger;
import feign.Request;
import feign.hc5.ApacheHttp5Client;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.atlas.libs.framework.api.client.rest.SSLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Value("${app.api-client.rest.ssl.enabled:false}")
  private boolean sslEnabled;

  @Bean
  public Client feignClient() throws Exception {
    PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();

    if (!sslEnabled) { // Note: Only for dev/test environments - security risk
      // Creates an SSL context that trusts all certificates
      SSLContext sslContext = SSLUtil.createTrustAllSSLContext();

      TlsSocketStrategy tlsSocketStrategy = (TlsSocketStrategy) ClientTlsStrategyBuilder.create()
          .setSslContext(sslContext)
          .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
          .build();

      connectionManagerBuilder.setTlsSocketStrategy(tlsSocketStrategy);
    } else {
      TlsSocketStrategy tlsSocketStrategy = (TlsSocketStrategy) ClientTlsStrategyBuilder.create()
          .setSslContext(SSLContexts.createSystemDefault())
          .build();

      connectionManagerBuilder.setTlsSocketStrategy(tlsSocketStrategy);
    }

    PoolingHttpClientConnectionManager connectionManager = connectionManagerBuilder
        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
        .setConnPoolPolicy(PoolReusePolicy.LIFO)
        .build();

    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .build();

    return new ApacheHttp5Client(httpClient);
  }

  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.HEADERS;
  }

  @Bean
  public Request.Options feignRequestOptions() {
    return new Request.Options(30, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true);
  }
}
