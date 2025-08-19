package org.atlas.edge.gateway.springcloudgateway.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.atlas.edge.gateway.springcloudgateway.security.jwt.JwtExtractor;
import org.atlas.framework.config.Application;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final ApplicationConfigPort applicationConfigPort;
  private final AuthRulesProps authRulesProps;
  private final CustomServerAuthenticationEntryPoint serverAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler accessDeniedHandler;
  private final JwtExtractor jwtExtractor;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(corsSpec -> corsSpec.configurationSource(exchange -> {
          CorsConfiguration corsConfig = new CorsConfiguration();

          // Configure allowed origins (domains/URLs that can send requests)
          corsConfig.setAllowedOrigins(
              applicationConfigPort.getConfigAsList(Application.SYSTEM, "cors.allowed-origins"));

          // Configure allowed HTTP methods
          corsConfig.setAllowedMethods(
              applicationConfigPort.getConfigAsList(Application.SYSTEM, "cors.allowed-methods"));

          // Configure allowed headers that can be sent from client
          corsConfig.setAllowedHeaders(
              applicationConfigPort.getConfigAsList(Application.SYSTEM, "cors.allowed-headers"));

          // Configure headers that are exposed from server to client
          List<String> exposedHeaders = applicationConfigPort.getConfigAsList(Application.SYSTEM, "cors.exposed-headers");
          if (CollectionUtils.isNotEmpty(exposedHeaders)) {
            exposedHeaders.forEach(corsConfig::addExposedHeader);
          }

          // Allow sending credentials (cookies, authorization headers) in CORS requests
          corsConfig.setAllowCredentials(
              applicationConfigPort.getConfigAsBoolean(Application.SYSTEM, "cors.allow-credentials", true));

          // Cache time for preflight requests (seconds), 0 = no cache
          corsConfig.setMaxAge(
              applicationConfigPort.getConfigAsLong(Application.SYSTEM, "cors.max-age", 0L));

          log.debug("CORS Configuration - Allowed Origins: {}, Allowed Methods: {}, Allowed Headers: {}, Exposed Headers: {}, Allow Credentials: {}, Max Age: {}",
              corsConfig.getAllowedOrigins(),
              corsConfig.getAllowedMethods(),
              corsConfig.getAllowedHeaders(),
              corsConfig.getExposedHeaders(),
              corsConfig.getAllowCredentials(),
              corsConfig.getMaxAge());
          return corsConfig;
        }))
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            // Handle JWT validation errors (e.g., expired token)
            .authenticationFailureHandler((webFilterExchange, exception) -> {
              // Delegate to CustomAuthenticationEntryPoint
              return serverAuthenticationEntryPoint.commence(webFilterExchange.getExchange(),
                  exception);
            }))
        .exceptionHandling(
            exceptionHandlingSpec -> exceptionHandlingSpec.authenticationEntryPoint(serverAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
        .authorizeExchange(auth -> {
          // Permit preflight requests
          auth.pathMatchers(HttpMethod.OPTIONS).permitAll();

          // Non-secured paths
          authRulesProps.getNonSecuredPaths().forEach(path -> auth.pathMatchers(path).permitAll());

          // Secured paths with role-based authorization
          authRulesProps.getSecuredPaths().forEach(rule -> auth.pathMatchers(rule.getPath())
              .hasAnyAuthority(rule.getRoles().toArray(String[]::new)));

          // Deny all other requests
          auth.anyExchange().denyAll();
        });

    return http.build();
  }

  @Bean
  public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
    ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      String roles = jwtExtractor.extractUserRoles(jwt);
      if (StringUtil.isNotBlank(roles)) {
        return Flux.fromArray(roles.split(","))
            .filter(StringUtils::isNotBlank)
            .map(SimpleGrantedAuthority::new);
      }
      return Flux.empty();
    });
    return converter;
  }
}
