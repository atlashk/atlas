package org.atlas.user.api.server.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.common.infrastructure.protobuf.user.ListUserRequestProto;
import org.atlas.common.infrastructure.protobuf.user.ListUserResponseProto;
import org.atlas.common.infrastructure.protobuf.user.UserProto;
import org.atlas.common.infrastructure.protobuf.user.UserServiceGrpc;
import org.atlas.user.application.internal.model.InternalRetrieveUserListInput;
import org.atlas.user.application.internal.service.InternalUserService;
import org.atlas.user.domain.entity.User;
import org.atlas.common.framework.collection.CollectionUtil;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private final InternalUserService internalUserService;

  @Override
  public void listUser(ListUserRequestProto requestProto,
      StreamObserver<ListUserResponseProto> responseObserver) {
    InternalRetrieveUserListInput input = map(requestProto);
    try {
      List<User> users = internalUserService.retrieveUserList(input);
      ListUserResponseProto responseProto = map(users);
      responseObserver.onNext(responseProto);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalRetrieveUserListInput map(ListUserRequestProto requestProto) {
    return new InternalRetrieveUserListInput(requestProto.getIdList());
  }

  private ListUserResponseProto map(List<User> users) {
    if (CollectionUtil.isEmpty(users)) {
      return ListUserResponseProto.getDefaultInstance();
    }
    ListUserResponseProto.Builder builder = ListUserResponseProto.newBuilder();
    users.forEach(user -> builder.addUser(map(user)));
    return builder.build();
  }

  private UserProto map(User user) {
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
