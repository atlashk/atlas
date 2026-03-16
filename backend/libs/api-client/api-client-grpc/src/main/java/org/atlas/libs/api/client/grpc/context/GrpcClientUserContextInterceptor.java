package org.atlas.libs.api.client.grpc.context;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.atlas.libs.framework.api.grpc.MetadataKeys;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalClientInterceptor
public class GrpcClientUserContextInterceptor implements ClientInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final Metadata.Key<String> AUTHORIZATION_HEADER =
      Metadata.Key.of(MetadataKeys.AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    Principal principal = SecurityContextUtil.getPrincipal();

    return new ForwardingClientCall.SimpleForwardingClientCall<>(
        next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        if (principal != null && StringUtil.isNotBlank(principal.getAccessToken())) {
          headers.put(AUTHORIZATION_HEADER, BEARER_PREFIX + principal.getAccessToken());
        }
        super.start(responseListener, headers);
      }
    };
  }
}
