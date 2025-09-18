package org.atlas.infrastructure.api.server.grpc.netdevh.core;

import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import org.atlas.framework.domain.exception.DomainException;

@GrpcAdvice
@Slf4j
public class GrpcExceptionHandler {

  @net.devh.boot.grpc.server.advice.GrpcExceptionHandler
  public Status handleException(Throwable e) {
    log.error("Occurred an exception", e);
    if (e instanceof DomainException) {
      return Status.INTERNAL
          .withDescription(e.getMessage())
          .withCause(e);
    } else {
      return Status.INTERNAL
          .withDescription("An internal server error occurred")
          .withCause(e);
    }
  }
}
