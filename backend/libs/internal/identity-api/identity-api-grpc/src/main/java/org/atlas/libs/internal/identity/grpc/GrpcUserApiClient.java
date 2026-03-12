package org.atlas.libs.internal.identity.grpc;

import java.util.List;
import org.atlas.libs.framework.internal.identity.client.UserApiClient;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.protobuf.identity.ListUserRequestProto;
import org.atlas.libs.protobuf.identity.ListUserResponseProto;
import org.atlas.libs.protobuf.identity.UserServiceGrpc;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcUserApiClient implements UserApiClient {

  private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

  public GrpcUserApiClient(GrpcChannelFactory channels) {
    this.userServiceBlockingStub =
        UserServiceGrpc.newBlockingStub(channels.createChannel("identity-service"));
  }

  @Override
  public List<UserOutput> call(RetrieveUserListInput request) {
    ListUserRequestProto requestProto = GrpcUserMapper.INSTANCE.map(request);
    ListUserResponseProto responseProto = userServiceBlockingStub.listUser(requestProto);
    return GrpcUserMapper.INSTANCE.map(responseProto);
  }
}
