package org.atlas.common.infrastructure.api.client.rest.restclient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.atlas.common.framework.security.SSLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Value("${app.api-client.rest.ssl.enabled:false}")
  private boolean sslEnabled;

  /**
   * Configures a RestClient with BufferingClientHttpRequestFactory to allow interceptors to read
   * the response body without consuming it. SSL validation can be disabled via properties.
   */
  @Bean
  public RestClient restClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    if (!sslEnabled) { // Note: Only for dev/test environments - security risk
      try {
        // Create an SSL context that trusts all certificates
        SSLContext sslContext = SSLUtil.createTrustAllSSLContext();

        // Create a custom HostnameVerifier that trusts all hostnames
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Set default SSLSocketFactory and HostnameVerifier
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      } catch (Exception e) {
        throw new RuntimeException("Failed to disable SSL validation", e);
      }
    }

    return RestClient.builder()
        .requestFactory(new BufferingClientHttpRequestFactory(requestFactory))
        .requestInterceptor(new LoggingRequestInterceptor())
        .requestInterceptor(new UserContextRequestInterceptor())
        .build();
  }
}
