package org.atlas.services.iam.api.server.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.protobuf.iam.ListUserRequestProto;
import org.atlas.libs.protobuf.iam.ListUserResponseProto;
import org.atlas.libs.protobuf.iam.UserProto;
import org.atlas.libs.protobuf.iam.UserServiceGrpc;
import org.atlas.services.iam.port.in.user.service.UserInternalService;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private final UserInternalService userInternalService;

  @Override
  public void listUser(ListUserRequestProto requestProto,
      StreamObserver<ListUserResponseProto> responseObserver) {
    RetrieveUserListInternalInput input = map(requestProto);
    try {
      List<InternalUserOutput> users = userInternalService.retrieveUserList(input);
      ListUserResponseProto responseProto = map(users);
      responseObserver.onNext(responseProto);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private RetrieveUserListInternalInput map(ListUserRequestProto requestProto) {
    return new RetrieveUserListInternalInput(requestProto.getIdList());
  }

  private ListUserResponseProto map(List<InternalUserOutput> users) {
    if (CollectionUtil.isEmpty(users)) {
      return ListUserResponseProto.getDefaultInstance();
    }
    ListUserResponseProto.Builder builder = ListUserResponseProto.newBuilder();
    users.forEach(user -> builder.addUser(map(user)));
    return builder.build();
  }

  private UserProto map(InternalUserOutput user) {
    return UserProto.newBuilder()
        .setId(user.getId())
        .setUsername(user.getUsername())
        .setFirstName(user.getFirstName())
        .setLastName(user.getLastName())
        .setEmail(user.getEmail())
        .setPhoneNumber(user.getPhoneNumber())
        .setRole(user.getRole().name())
        .build();
  }
}
