package org.atlas.infrastructure.api.client.grpc.netdevh;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

@GrpcGlobalClientInterceptor
@Slf4j
public class GrpcClientLoggingInterceptor implements ClientInterceptor {

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions,
      Channel next) {
    log.info("Received call to {}", method.getFullMethodName());
    final long startTime = System.currentTimeMillis();
    return new ForwardingClientCall.SimpleForwardingClientCall<>(
        next.newCall(method, callOptions)) {

      @Override
      public void sendMessage(ReqT message) {
        log.debug("Request message: {}", limitLogMessage(message));
        super.sendMessage(message);
      }

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(
            new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                responseListener) {
              @Override
              public void onMessage(RespT message) {
                log.debug("ApiResponseWrapper message: {}", limitLogMessage(message));
                super.onMessage(message);
              }

              @Override
              public void onHeaders(Metadata headers) {
                log.debug("gRPC headers: {}", headers);
                super.onHeaders(headers);
              }

              @Override
              public void onClose(Status status, Metadata trailers) {
                long duration = System.currentTimeMillis() - startTime;
                if (status.isOk()) {
                  log.info("Call to {} completed successfully in {}ms", method.getFullMethodName(),
                      duration);
                } else {
                  log.error("Call to {} failed with status: {} after {}ms",
                      method.getFullMethodName(), status, duration);
                }
                log.debug("Trailers: {}", trailers);
                super.onClose(status, trailers);
              }
            }, headers);
      }
    };
  }

  private String limitLogMessage(Object message) {
    String stringMessage = String.valueOf(message);
    return stringMessage.length() > 100 ? stringMessage.substring(0, 100) + "..." : stringMessage;
  }
}
