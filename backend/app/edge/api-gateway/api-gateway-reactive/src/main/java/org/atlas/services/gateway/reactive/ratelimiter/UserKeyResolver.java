package org.atlas.services.gateway.reactive.ratelimiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.gateway.reactive.util.IpAddressUtil;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserKeyResolver implements KeyResolver {

  private static final String ANONYMOUS_KEY = "anonymous";

  @Override
  public Mono<String> resolve(ServerWebExchange exchange) {
    String ipAddress = IpAddressUtil.getIpAddress(exchange.getRequest());
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(auth ->
            auth != null && auth.isAuthenticated() && auth.getCredentials() instanceof Jwt)
        .map(auth -> (Jwt) auth.getCredentials())
        .map(jwt -> {
          String userId = jwt.getSubject();
          if (StringUtil.isNotBlank(userId)) {
            return userId + ":" + ipAddress;
          }
          return ANONYMOUS_KEY + ":" + ipAddress;
        })
        .defaultIfEmpty(ANONYMOUS_KEY + ":" + ipAddress);
  }
}
