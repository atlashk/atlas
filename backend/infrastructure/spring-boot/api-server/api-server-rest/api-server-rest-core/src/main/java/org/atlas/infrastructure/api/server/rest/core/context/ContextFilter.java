package org.atlas.infrastructure.api.server.rest.core.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.atlas.framework.util.RoleUtil;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.auth.enums.CustomClaim;
import org.atlas.infrastructure.api.server.rest.core.util.HttpUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class ContextFilter extends OncePerRequestFilter {

  // Only apply context filter to /api/** routes
  private static final Pattern FILTERED_PATHS = Pattern.compile("^/api(/.*)?$");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    ContextInfo contextInfo = new ContextInfo();

    // For authorized requests
    final String sessionIdHeader = HttpUtil.getHeader(request, CustomClaim.SESSION_ID.getHeader());
    final String userIdHeader = HttpUtil.getHeader(request, CustomClaim.USER_ID.getHeader());
    final String userRolesHeader = HttpUtil.getHeader(request, CustomClaim.USER_ROLES.getHeader());
    final String expiresAtHeader = HttpUtil.getHeader(request, CustomClaim.EXPIRES_AT.getHeader());
    if (StringUtils.isNotBlank(sessionIdHeader) &&
        StringUtils.isNotBlank(userIdHeader) &&
        StringUtils.isNotBlank(userRolesHeader) &&
        StringUtils.isNotBlank(expiresAtHeader)) {
      contextInfo.setSessionId(sessionIdHeader);
      contextInfo.setUserId(Integer.parseInt(userIdHeader));
      contextInfo.setUserRoles(RoleUtil.toRolesSet(userRolesHeader));
      contextInfo.setExpiresAt(new Date(Long.parseLong(expiresAtHeader)));
    }

    // Device ID
    final String deviceIdHeader = HttpUtil.getHeader(request, "X-Device-Id");
    contextInfo.setDeviceId(deviceIdHeader);

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
