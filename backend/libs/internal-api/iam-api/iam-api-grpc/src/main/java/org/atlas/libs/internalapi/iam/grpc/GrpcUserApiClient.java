package org.atlas.libs.internalapi.iam.grpc;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.atlas.libs.framework.internalapi.iam.client.UserApiClient;
import org.atlas.libs.framework.internalapi.iam.model.RetrieveUserListInput;
import org.atlas.libs.framework.internalapi.iam.model.UserOutput;
import org.atlas.libs.protobuf.iam.ListUserRequestProto;
import org.atlas.libs.protobuf.iam.ListUserResponseProto;
import org.atlas.libs.protobuf.iam.UserServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Retry(name = "default")
@CircuitBreaker(name = "default")
@Bulkhead(name = "default")
public class GrpcUserApiClient implements UserApiClient {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

  @Override
  public List<UserOutput> call(RetrieveUserListInput request) {
    ListUserRequestProto requestProto = GrpcUserMapper.INSTANCE.map(request);
    ListUserResponseProto responseProto = userServiceBlockingStub.listUser(requestProto);
    return GrpcUserMapper.INSTANCE.map(responseProto);
  }
}
