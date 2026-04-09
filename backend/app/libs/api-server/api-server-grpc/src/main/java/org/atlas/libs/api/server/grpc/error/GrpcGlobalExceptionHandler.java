package org.atlas.libs.api.server.grpc.error;

import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GrpcGlobalExceptionHandler implements GrpcExceptionHandler {

  @Override
  public StatusException handleException(Throwable exception) {
    log.error("Occurred an exception", exception);
    Status status;
    if (exception instanceof DomainException) {
      status = Status.INTERNAL
          .withDescription(exception.getMessage())
          .withCause(exception);
    } else {
      status = Status.INTERNAL
          .withDescription("An internal server error occurred")
          .withCause(exception);
    }
    return new StatusException(status);
  }
}
