package org.atlas.services.gateway.reactive.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.util.CollectionUtil;
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
public class SecurityConfig {

  private final ApplicationConfigService applicationConfigService;
  private final CustomServerAuthenticationEntryPoint serverAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(corsSpec -> corsSpec.configurationSource(exchange -> {
          CorsConfiguration corsConfig = new CorsConfiguration();

          // Configure allowed origins (domains/URLs that can send requests)
          corsConfig.setAllowedOrigins(
              applicationConfigService.getConfigAsList("security.cors.allowed-origins"));

          // Configure allowed HTTP methods
          corsConfig.setAllowedMethods(
              applicationConfigService.getConfigAsList("security.cors.allowed-methods"));

          // Configure allowed headers that can be sent from client
          corsConfig.setAllowedHeaders(
              applicationConfigService.getConfigAsList("security.cors.allowed-headers"));

          // Configure headers that are exposed from server to client
          List<String> exposedHeaders =
              applicationConfigService.getConfigAsList("security.cors.exposed-headers");
          if (CollectionUtil.isNotEmpty(exposedHeaders)) {
            exposedHeaders.forEach(corsConfig::addExposedHeader);
          }

          // Allow sending credentials (cookies, authorization headers) in CORS requests
          corsConfig.setAllowCredentials(
              applicationConfigService.getConfigAsBoolean("security.cors.allow-credentials", true));

          // Cache time for preflight requests (seconds), 0 = no cache
          corsConfig.setMaxAge(
              applicationConfigService.getConfigAsLong("security.cors.max-age", 0L));

          return corsConfig;
        }))
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
}
