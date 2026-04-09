package org.atlas.edge.gateway.reactive.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class ApiGatewaySecurityConfig {

  private final ApplicationConfigService applicationConfigService;
  private final CustomServerAuthenticationEntryPoint serverAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(corsSpec -> corsSpec.configurationSource(exchange -> buildCorsConfiguration()))
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(Customizer.withDefaults()) // Handle JWT validation errors (e.g., expired token)
            .authenticationFailureHandler((webFilterExchange, exception) -> {
              // Delegate to CustomAuthenticationEntryPoint
              return serverAuthenticationEntryPoint.commence(webFilterExchange.getExchange(),
                  exception);
            }))
        .exceptionHandling(
            exceptionHandlingSpec -> exceptionHandlingSpec.authenticationEntryPoint(
                    serverAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

    return http.build();
  }

  private CorsConfiguration buildCorsConfiguration() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        applicationConfigService.getConfigAsList("security.cors.allowed-origins"));
    configuration.setAllowedMethods(
        applicationConfigService.getConfigAsList("security.cors.allowed-methods"));
    configuration.setAllowedHeaders(
        applicationConfigService.getConfigAsList("security.cors.allowed-headers"));
    configuration.setExposedHeaders(
        applicationConfigService.getConfigAsList("security.cors.exposed-headers"));
    configuration.setAllowCredentials(
        applicationConfigService.getConfigAsBoolean("security.cors.allow-credentials", true));
    configuration.setMaxAge(
        applicationConfigService.getConfigAsLong("security.cors.max-age", 0L));
    return configuration;
  }
}
