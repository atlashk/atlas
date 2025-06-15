package org.atlas.edge.gateway.springcloudgateway.security;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.atlas.edge.gateway.springcloudgateway.security.jwt.JwtExtractor;
import org.atlas.framework.config.Application;
import org.atlas.framework.config.ApplicationConfigPort;
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
        .cors(corsSpec ->
            corsSpec.configurationSource(exchange -> {
              CorsConfiguration corsConfig = new CorsConfiguration();
              corsConfig.setAllowedOrigins(
                  applicationConfigPort.getConfigAsList(Application.SYSTEM,
                      "cors.allowed-origins"));
              corsConfig.setAllowedMethods(
                  applicationConfigPort.getConfigAsList(Application.SYSTEM,
                      "cors.allowed-methods"));
              corsConfig.setAllowedHeaders(
                  applicationConfigPort.getConfigAsList(Application.SYSTEM,
                      "cors.allowed-headers"));
              corsConfig.setAllowCredentials(
                  applicationConfigPort.getConfigAsBoolean(Application.SYSTEM,
                      "cors.allow-credentials"));
              corsConfig.setMaxAge(
                  applicationConfigPort.getConfigAsLong(Application.SYSTEM, "cors.max-age"));
              return corsConfig;
            }))
        .oauth2ResourceServer(oauth2 ->
            oauth2
                .jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                // Handle JWT validation errors (e.g., expired token)
                .authenticationFailureHandler((webFilterExchange, exception) -> {
                  // Delegate to CustomAuthenticationEntryPoint
                  return serverAuthenticationEntryPoint.commence(webFilterExchange.getExchange(),
                      exception);
                })
        )
        .exceptionHandling(exceptionHandlingSpec ->
            exceptionHandlingSpec.authenticationEntryPoint(serverAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        )
        .authorizeExchange(auth -> {
          // Permit preflight requests
          auth.pathMatchers(HttpMethod.OPTIONS).permitAll();

          // Non-secured paths
          authRulesProps.getNonSecuredPaths().forEach(path ->
              auth.pathMatchers(path).permitAll()
          );

          // Secured paths with role-based authorization
          authRulesProps.getSecuredPaths().forEach(rule ->
              auth.pathMatchers(rule.getPath())
                  .hasAnyAuthority(rule.getRoles().toArray(String[]::new))
          );

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
      if (StringUtils.isNotBlank(roles)) {
        return Flux.fromArray(roles.split(","))
            .filter(StringUtils::isNotBlank)
            .map(SimpleGrantedAuthority::new);
      }
      return Flux.empty();
    });
    return converter;
  }
}
