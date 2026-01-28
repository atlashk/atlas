package org.atlas.common.infrastructure.api.server.rest.core.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.atlas.common.framework.domain.user.Role;
import org.atlas.common.framework.context.ContextInfo;
import org.atlas.common.framework.context.Contexts;
import org.atlas.common.framework.security.CustomClaim;
import org.atlas.common.framework.util.StringUtil;
import org.atlas.common.infrastructure.api.server.rest.core.util.HttpUtil;
import org.atlas.common.infrastructure.api.server.rest.core.util.IpAddressUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class UserContextFilter extends OncePerRequestFilter {

  // Only apply context filter to /api/** routes
  private static final Pattern FILTERED_PATHS = Pattern.compile("^/api(/.*)?$");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    ContextInfo contextInfo = new ContextInfo();

    // Client IP address
    contextInfo.setIpAddress(IpAddressUtil.getIpAddress(request));

    // For authorized requests
    final String userIdHeader = HttpUtil.getHeader(request, CustomClaim.USER_ID.getHeader());
    final String userRoleHeader = HttpUtil.getHeader(request, CustomClaim.USER_ROLE.getHeader());
    if (StringUtil.isNotBlank(userIdHeader) &&
        StringUtil.isNotBlank(userRoleHeader)) {
      contextInfo.setUserId(Integer.parseInt(userIdHeader));
      contextInfo.setUserRole(Role.valueOf(userRoleHeader));
    }

    try {
      Contexts.set(contextInfo);
      filterChain.doFilter(request, response);
    } finally {
      // Clean up to prevent memory leak
      Contexts.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Only apply the filter if the URI matches /api/**
    return !FILTERED_PATHS.matcher(request.getRequestURI()).matches();
  }
}
