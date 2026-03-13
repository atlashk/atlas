package org.atlas.libs.api.client.rest.restclient.config;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.api.client.rest.restclient.logging.RestClientLoggingInterceptor;
import org.atlas.libs.api.client.rest.restclient.context.RestClientUserContextInterceptor;
import org.atlas.libs.framework.api.client.rest.SSLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
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
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

    if (apiClientRestProps == null || !apiClientRestProps.getSsl().getEnabled()) { // Note: Only for dev/test environments - security risk
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
        .requestInterceptor(new RestClientLoggingInterceptor())
        .requestInterceptor(new RestClientUserContextInterceptor())
        .build();
  }
}
