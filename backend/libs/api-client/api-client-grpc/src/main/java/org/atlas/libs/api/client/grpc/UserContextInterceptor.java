package org.atlas.libs.api.client.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.atlas.libs.framework.context.ContextInfo;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.security.CustomClaim;

@GrpcGlobalClientInterceptor
public class UserContextInterceptor implements ClientInterceptor {

  private static final Metadata.Key<String> USER_ID_HEADER =
      Metadata.Key.of(CustomClaim.USER_ID.getHeader(), Metadata.ASCII_STRING_MARSHALLER);
  private static final Metadata.Key<String> USER_ROLE_HEADER =
      Metadata.Key.of(CustomClaim.USER_ROLE.getHeader(), Metadata.ASCII_STRING_MARSHALLER);

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
          headers.put(USER_ID_HEADER, contextInfo.getUserId());
          headers.put(USER_ROLE_HEADER, contextInfo.getUserRole().name());
        }
        super.start(responseListener, headers);
      }
    };
  }
}
