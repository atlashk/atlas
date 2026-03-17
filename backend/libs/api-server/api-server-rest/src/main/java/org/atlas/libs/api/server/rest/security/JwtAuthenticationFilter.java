package org.atlas.libs.api.server.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.atlas.libs.api.server.rest.util.IpAddressUtil;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.CustomClaim;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.security.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    Principal principal = new Principal();

    // Client IP address
    principal.setIpAddress(IpAddressUtil.getIpAddress(request));

    // Handle access token if presents
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = JwtUtil.extractBearerToken(authorization);
    if (StringUtil.isNotBlank(accessToken)) {
      principal.setAccessToken(accessToken);
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

      // Set Principal into context
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          principal, null, principal.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
