package org.atlas.infrastructure.notification.email.ses.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@Slf4j
public class SesConfig implements DisposableBean {

  private SesV2Client sesClient;

  @Bean
  public SesV2Client sesClient() {
    this.sesClient = SesV2Client.builder()
        .build();
    log.info("Initialized SES client");
    return this.sesClient;
  }

  @Override
  public void destroy() {
    if (this.sesClient != null) {
      try {
        this.sesClient.close();
        log.info("SES client closed successfully");
      } catch (Exception e) {
        log.error("An error occurred while closing the SES client", e);
      }
    }
  }
}
