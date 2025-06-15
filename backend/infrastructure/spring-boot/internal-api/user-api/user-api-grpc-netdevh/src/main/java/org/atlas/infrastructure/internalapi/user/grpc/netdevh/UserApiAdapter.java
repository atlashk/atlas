package org.atlas.infrastructure.internalapi.user.grpc.netdevh;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.framework.internalapi.user.UserApiPort;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.UserProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.UserServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class UserApiAdapter implements UserApiPort {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

  @Override
  public List<UserResponse> call(ListUserRequest request) {
    ListUserRequestProto requestProto = map(request);
    ListUserResponseProto responseProto = userServiceBlockingStub.listUser(requestProto);
    return map(responseProto);
  }

  private ListUserRequestProto map(ListUserRequest request) {
    return ListUserRequestProto.newBuilder()
        .addAllId(request.getIds())
        .build();
  }

  private List<UserResponse> map(ListUserResponseProto responseProto) {
    return responseProto.getUserList()
        .stream()
        .map(this::map)
        .toList();
  }

  private UserResponse map(UserProto userProto) {
    UserResponse userResponse = ObjectMapperUtil.getInstance()
        .map(userProto, UserResponse.class);
    userResponse.setRole(Role.valueOf(userProto.getRole()));
    return userResponse;
  }
}
