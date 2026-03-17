package org.atlas.libs.api.server.grpc.context;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.api.grpc.MetadataKeys;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.CustomClaim;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.security.JwtUtil;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
@Slf4j
public class GrpcServerUserContextInterceptor implements ServerInterceptor {

  private static final Metadata.Key<String> AUTHORIZATION_HEADER =
      Metadata.Key.of(MetadataKeys.AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
      Metadata metadata,
      ServerCallHandler<ReqT, RespT> serverCallHandler) {
    // Extract access token from gRPC metadata
    String authorization = metadata.get(AUTHORIZATION_HEADER);
    String accessToken = JwtUtil.extractBearerToken(authorization);

    // Parse access token and set security context
    if (StringUtil.isNotBlank(accessToken)) {
      Principal principal = new Principal();
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

      String userId = principal.getUserId();
      UserRole userRole = principal.getUserRole();
      if (StringUtil.isNotBlank(userId) && userRole != null) {
        SecurityContextUtil.setContext(principal);
      }
    }

    return serverCallHandler.startCall(serverCall, metadata);
  }
}
