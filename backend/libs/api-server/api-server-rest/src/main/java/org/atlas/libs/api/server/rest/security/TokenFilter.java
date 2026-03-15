package org.atlas.libs.api.server.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.AuthContext;
import org.atlas.libs.framework.security.CustomClaim;
import org.atlas.libs.api.server.rest.util.IpAddressUtil;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.jwt.JwtUtil;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class TokenFilter extends OncePerRequestFilter {

  // Only apply context filter to /api/** routes
  private static final Pattern FILTERED_PATHS = Pattern.compile("^/api(/.*)?$");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    Principal principal = new Principal();

    // Client IP address
    principal.setIpAddress(IpAddressUtil.getIpAddress(request));

    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
      String accessToken = authorization.substring("Bearer ".length()).trim();
      if (StringUtil.isNotBlank(accessToken)) {
        try {
          principal.setUserId(JwtUtil.extractSubject(accessToken));
          JwtUtil.<UserRole>extractClaim(accessToken, CustomClaim.USER_ROLE)
              .ifPresent(principal::setUserRole);
          JwtUtil.<String>extractClaim(accessToken, CustomClaim.FIRST_NAME)
              .ifPresent(principal::setFirstName);
          JwtUtil.<String>extractClaim(accessToken, CustomClaim.LAST_NAME)
              .ifPresent(principal::setLastName);
          JwtUtil.<String>extractClaim(accessToken, CustomClaim.EMAIL)
              .ifPresent(principal::setEmail);
          JwtUtil.<String>extractClaim(accessToken, CustomClaim.PHONE)
              .ifPresent(principal::setPhone);
        } catch (Exception ignored) {
        }
      }
    }

    try {
      AuthContext.setPrincipal(principal);
      filterChain.doFilter(request, response);
    } finally {
      // Clean up to prevent memory leak
      AuthContext.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Only apply the filter if the URI matches /api/**
    return !FILTERED_PATHS.matcher(request.getRequestURI()).matches();
  }
}
