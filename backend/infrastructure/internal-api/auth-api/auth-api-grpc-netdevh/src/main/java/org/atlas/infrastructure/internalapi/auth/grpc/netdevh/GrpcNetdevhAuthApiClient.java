package org.atlas.infrastructure.internalapi.auth.grpc.netdevh;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.atlas.infrastructure.api.server.grpc.protobuf.auth.CreateUserRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.auth.UserServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class GrpcNetdevhAuthApiClient implements AuthApiClient {

  @GrpcClient("auth-server")
  private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

  @Override
  public void createUser(CreateUserRequest request) {
    CreateUserRequestProto requestProto = GrpcAuthMapper.INSTANCE.toCreateUserRequestProto(request);
    userServiceBlockingStub.createUser(requestProto);
  }
}
