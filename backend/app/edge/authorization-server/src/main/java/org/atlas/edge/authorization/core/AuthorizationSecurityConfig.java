package org.atlas.edge.authorization.core;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.security.cryptography.RsaKeyLoader;
import org.atlas.libs.framework.security.jwt.Claim;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.edge.authorization.core.sso.SsoAuthenticationFailureHandler;
import org.atlas.edge.authorization.core.sso.SsoAuthenticationSuccessHandler;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.domain.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class AuthorizationSecurityConfig {

  private final ApplicationConfigService applicationConfigService;
  private final UserDetailsService userDetailsService;
  private final OneTimeTokenService oneTimeTokenService;
  private final UserRepository userRepository;
  private final SsoAuthenticationSuccessHandler ssoAuthenticationSuccessHandler;
  private final SsoAuthenticationFailureHandler ssoAuthenticationFailureHandler;

  /**
   * Configures the security filter chain for the authorization server.
   */
  @Bean
  @Order(0)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
      throws Exception {
    return http.oauth2AuthorizationServer((authorizationServer) -> {
          http.securityMatcher(authorizationServer.getEndpointsMatcher());
          authorizationServer.oidc(Customizer.withDefaults());
        })
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
        .exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
            new LoginUrlAuthenticationEntryPoint("/login"),
            new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
        .build();
  }

  /**
   * Configures the security filter chain for JWT-based authentication APIs.
   */
  @Bean
  @Order(1)
  public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/api/authentication/**")
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth ->
            auth.anyRequest().permitAll())
        .sessionManagement((session) ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> {
          ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
          ex.accessDeniedHandler(new CustomAccessDeniedHandler());
        })
        .build();
  }

  /**
   * Configures the security filter chain for SSO login endpoints.
   */
  @Bean
  @Order(2)
  public SecurityFilterChain ssoSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher(
            "/oauth2/**",
            "/login/oauth2/**"
        )
        .authorizeHttpRequests(auth ->
            auth.anyRequest().permitAll())
        .oauth2Login(oauth2 -> oauth2 // SSO login
            .successHandler(ssoAuthenticationSuccessHandler)
            .failureHandler(ssoAuthenticationFailureHandler))
        .sessionManagement((session) ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  /**
   * Configures the security filter chain for OAuth2 login form.
   */
  @Bean
  @Order(3)
  public SecurityFilterChain oauth2LoginFormSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/login", "/login/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .failureUrl("/login?error")
            .permitAll())
        .build();
  }

  @Bean
  public JWKSource<SecurityContext> jwkSource() throws Exception {
    RSAPublicKey rsaPublicKey = RsaKeyLoader.loadPublicKey("token/token.pub");
    RSAPrivateKey rsaPrivateKey = RsaKeyLoader.loadPrivateKey("token/token.key");
    RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
        .privateKey(rsaPrivateKey)
        .keyID(SecurityConstant.JWKS_KEY_ID)
        .build();
    return new ImmutableJWKSet<>(new JWKSet(rsaKey));
  }

  @Bean
  public OAuth2TokenGenerator<?> oAuth2TokenGenerator(
      JWKSource<SecurityContext> jwkSource,
      OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer) {
    JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
    JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
    jwtGenerator.setJwtCustomizer(jwtTokenCustomizer);
    OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
    PublicClientRefreshTokenGenerator refreshTokenGenerator =
        new PublicClientRefreshTokenGenerator(userRepository);
    return new DelegatingOAuth2TokenGenerator(
        jwtGenerator,
        accessTokenGenerator,
        refreshTokenGenerator);
  }

  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer() {
    return context -> {
      if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
        return;
      }
      String username = context.getPrincipal().getName();
      Optional<User> optionalUser = userRepository.findByEmail(username);
      if (optionalUser.isEmpty()) {
        return;
      }
      User user = optionalUser.get();
      context.getClaims().subject(user.getId());
      if (context.getAuthorizedScopes().contains("profile")) {
        context.getClaims().claim(Claim.FIRST_NAME.getClaimName(), user.getFirstName());
        context.getClaims().claim(Claim.LAST_NAME.getClaimName(), user.getLastName());
      }
      if (context.getAuthorizedScopes().contains("email")) {
        context.getClaims().claim(Claim.EMAIL.getClaimName(), user.getEmail());
      }
      if (context.getAuthorizedScopes().contains("phone")) {
        context.getClaims().claim(Claim.PHONE_NUMBER.getClaimName(), user.getPhoneNumber());
      }
      context.getClaims().claim(Claim.USER_ROLE.getClaimName(), user.getRole());
    };
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = buildCorsConfiguration();
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(
        userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
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
