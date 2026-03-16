package org.atlas.libs.api.client.grpc.context;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityContext;
import org.atlas.libs.framework.security.CustomClaim;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalClientInterceptor
public class GrpcClientUserContextInterceptor implements ClientInterceptor {

  private static final Metadata.Key<String> USER_ID_HEADER =
      Metadata.Key.of(CustomClaim.USER_ID.getHeader(), Metadata.ASCII_STRING_MARSHALLER);
  private static final Metadata.Key<String> USER_ROLE_HEADER =
      Metadata.Key.of(CustomClaim.USER_ROLE.getHeader(), Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions,
      Channel next) {
    Principal principal = SecurityContext.get();
    return new ForwardingClientCall.SimpleForwardingClientCall<>(
        next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        if (principal != null) {
          headers.put(USER_ID_HEADER, principal.getUserId());
          headers.put(USER_ROLE_HEADER, principal.getUserRole().name());
        }
        super.start(responseListener, headers);
      }
    };
  }
}
