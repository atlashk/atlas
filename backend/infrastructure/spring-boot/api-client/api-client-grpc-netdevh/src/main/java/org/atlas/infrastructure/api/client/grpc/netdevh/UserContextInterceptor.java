package org.atlas.infrastructure.api.client.grpc.netdevh;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.auth.enums.CustomClaim;
import org.atlas.framework.util.RoleUtil;

@GrpcGlobalClientInterceptor
public class UserContextInterceptor implements ClientInterceptor {

  private static final Metadata.Key<String> SESSION_ID_HEADER =
      Metadata.Key.of(CustomClaim.SESSION_ID.getHeader(), Metadata.ASCII_STRING_MARSHALLER);
  private static final Metadata.Key<String> USER_ID_HEADER =
      Metadata.Key.of(CustomClaim.USER_ID.getHeader(), Metadata.ASCII_STRING_MARSHALLER);
  private static final Metadata.Key<String> USER_ROLES_HEADER =
      Metadata.Key.of(CustomClaim.USER_ROLES.getHeader(), Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions,
      Channel next) {
    ContextInfo contextInfo = Contexts.get();
    return new ForwardingClientCall.SimpleForwardingClientCall<>(
        next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        if (contextInfo != null) {
          headers.put(SESSION_ID_HEADER, contextInfo.getSessionId());
          headers.put(USER_ID_HEADER, String.valueOf(contextInfo.getUserId()));
          headers.put(USER_ROLES_HEADER, RoleUtil.toRolesString(contextInfo.getUserRoles()));
        }
        super.start(responseListener, headers);
      }
    };
  }
}
