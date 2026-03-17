package org.atlas.platform.authorization.spring.application.core;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.platform.authorization.spring.application.core.oauth2.OAuth2AuthenticationFailureHandler;
import org.atlas.platform.authorization.spring.application.core.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationProvider;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class AuthorizationSecurityConfig {

  private final UserDetailsService userDetailsService;
  private final OneTimeTokenService oneTimeTokenService;
  private final ApplicationConfigService applicationConfigService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

  @Bean
  @Order(0)
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher(
            "/api/authentication/**", // Default flow
            "/oauth2/**",             // OAuth2 authorization
            "/login/oauth2/**"        // OAuth2 callback
        )
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth ->
            auth.anyRequest().permitAll())
        .oauth2Login(oauth2 -> oauth2
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler))
        .sessionManagement((session) ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> {
          // Unauthorized
          ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
          // Access denied
          ex.accessDeniedHandler(new CustomAccessDeniedHandler());
        })
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = buildCorsConfiguration();
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(
        userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationProvider oneTimeTokenAuthenticationProvider() {
    return new OneTimeTokenAuthenticationProvider(oneTimeTokenService, userDetailsService);
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config,
      AuthenticationProvider daoAuthenticationProvider,
      AuthenticationProvider oneTimeTokenAuthenticationProvider) throws Exception {
    // Manually configure AuthenticationManager with both providers
    return new ProviderManager(daoAuthenticationProvider, oneTimeTokenAuthenticationProvider);
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
