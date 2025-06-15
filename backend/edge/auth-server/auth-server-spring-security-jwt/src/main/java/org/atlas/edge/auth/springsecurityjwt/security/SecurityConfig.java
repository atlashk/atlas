package org.atlas.edge.auth.springsecurityjwt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationProvider;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(
    securedEnabled = true, // Enables @Secured annotation
    jsr250Enabled = true,  // Enables @RolesAllowed annotation
    prePostEnabled = true  // Enables @PreAuthorize, @PostAuthorize, @PreFilter, @PostFilter annotations
)
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final OneTimeTokenService oneTimeTokenService;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth ->
            auth.anyRequest().permitAll())
        .sessionManagement((session) ->
            // No session should be created
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
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
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
}
