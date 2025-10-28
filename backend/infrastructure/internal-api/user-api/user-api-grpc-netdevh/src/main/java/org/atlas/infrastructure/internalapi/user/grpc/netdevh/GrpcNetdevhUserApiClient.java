package org.atlas.infrastructure.internalapi.user.grpc.netdevh;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.framework.internalapi.user.UserApiClient;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.UserServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class GrpcNetdevhUserApiClient implements UserApiClient {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

  @Override
  public List<UserResponse> call(ListUserRequest request) {
    ListUserRequestProto requestProto = GrpcUserMapper.INSTANCE.map(request);
    ListUserResponseProto responseProto = userServiceBlockingStub.listUser(requestProto);
    return GrpcUserMapper.INSTANCE.map(responseProto);
  }
}
