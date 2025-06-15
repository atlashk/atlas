package org.atlas.infrastructure.storage.s3.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
public class S3Config implements DisposableBean {

  private S3Client s3Client;
  private S3Presigner s3Presigner;

  @Bean
  public S3Client s3Client() {
    this.s3Client = S3Client.builder()
        .build();
    log.info("Initialized S3 client");
    return this.s3Client;
  }

  @Bean
  public S3Presigner s3Presigner() {
    this.s3Presigner = S3Presigner.create();
    log.info("Initialized S3 presigner");
    return this.s3Presigner;
  }

  @Override
  public void destroy() throws Exception {
    if (this.s3Client != null) {
      this.s3Client.close();
      log.info("Closed S3 client");
    }
    if (this.s3Presigner != null) {
      this.s3Presigner.close();
      log.info("Closed S3 presigner");
    }
  }
}
