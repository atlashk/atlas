package org.atlas.common.infrastructure.api.server.grpc.core;

import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcAuthenticationConfig {

  @Bean
  public GrpcAuthenticationReader grpcAuthenticationReader() {
    return new BasicGrpcAuthenticationReader();
  }
}
