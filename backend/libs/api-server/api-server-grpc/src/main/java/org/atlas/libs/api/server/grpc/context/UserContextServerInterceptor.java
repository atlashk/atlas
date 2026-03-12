package org.atlas.libs.api.server.grpc.context;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.context.ContextInfo;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.CustomClaim;
import org.atlas.libs.framework.util.StringUtil;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
@Slf4j
public class UserContextServerInterceptor implements ServerInterceptor {

  private static final Metadata.Key<String> USER_ID_HEADER =
      Metadata.Key.of(CustomClaim.USER_ID.getHeader(), Metadata.ASCII_STRING_MARSHALLER);
  private static final Metadata.Key<String> USER_ROLE_HEADER =
      Metadata.Key.of(CustomClaim.USER_ROLE.getHeader(), Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
      Metadata metadata,
      ServerCallHandler<ReqT, RespT> serverCallHandler) {
    final String userIdHeader = metadata.get(USER_ID_HEADER);
    final String userRoleHeader = metadata.get(USER_ROLE_HEADER);
    if (StringUtil.isNotBlank(userIdHeader) &&
        StringUtil.isNotBlank(userRoleHeader)) {
      ContextInfo contextInfo = new ContextInfo();
      contextInfo.setUserId(userIdHeader);
      contextInfo.setUserRole(UserRole.valueOf(userRoleHeader));
      Contexts.set(contextInfo);
    }

    try {
      return serverCallHandler.startCall(serverCall, metadata);
    } finally {
      // Clean up to prevent memory leak
      Contexts.clear();
    }
  }
}
