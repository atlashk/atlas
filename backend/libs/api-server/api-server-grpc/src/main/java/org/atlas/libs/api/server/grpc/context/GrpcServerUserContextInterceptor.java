package org.atlas.libs.api.server.grpc.context;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.api.grpc.MetadataKeys;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.jwt.JwtDecoder;
import org.atlas.libs.framework.security.jwt.JwtUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.api.server.grpc.util.GrpcIpAddressUtil;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
@Order(1)
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

    if (StringUtil.isNotBlank(accessToken)) {
      // Decode access token to Principal
      Principal principal = JwtDecoder.decode(accessToken);

      // Client IP address
      principal.setIpAddress(GrpcIpAddressUtil.getIpAddress(serverCall, metadata));

      // Set Principal into context
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          principal, null, principal.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    return serverCallHandler.startCall(serverCall, metadata);
  }
}
