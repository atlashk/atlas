package org.atlas.libs.api.server.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.atlas.libs.api.server.rest.util.IpAddressUtil;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.jwt.JwtDecoder;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.security.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    // Handle access token if presents
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = JwtUtil.extractBearerToken(authorization);
    if (StringUtil.isNotBlank(accessToken)) {
      // Decode access token to Principal
      Principal principal = JwtDecoder.decode(accessToken);

      // Client IP address
      principal.setIpAddress(IpAddressUtil.getIpAddress(request));

      // Set Principal into context
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          principal, null, principal.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}
