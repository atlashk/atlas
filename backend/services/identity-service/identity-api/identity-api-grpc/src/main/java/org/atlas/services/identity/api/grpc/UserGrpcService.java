package org.atlas.services.identity.api.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.protobuf.identity.ListUserRequestProto;
import org.atlas.libs.protobuf.identity.ListUserResponseProto;
import org.atlas.libs.protobuf.identity.UserProto;
import org.atlas.libs.protobuf.identity.UserServiceGrpc;
import org.atlas.services.identity.port.in.user.service.UserInternalService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private final UserInternalService userInternalService;

  @Override
  public void listUser(ListUserRequestProto requestProto,
      StreamObserver<ListUserResponseProto> responseObserver) {
    RetrieveUserListInput input = map(requestProto);
    try {
      List<UserOutput> users = userInternalService.retrieveUserList(input);
      ListUserResponseProto responseProto = map(users);
      responseObserver.onNext(responseProto);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private RetrieveUserListInput map(ListUserRequestProto requestProto) {
    return new RetrieveUserListInput(requestProto.getIdList());
  }

  private ListUserResponseProto map(List<UserOutput> users) {
    if (CollectionUtil.isEmpty(users)) {
      return ListUserResponseProto.getDefaultInstance();
    }
    ListUserResponseProto.Builder builder = ListUserResponseProto.newBuilder();
    users.forEach(user -> builder.addUser(map(user)));
    return builder.build();
  }

  private UserProto map(UserOutput user) {
    return UserProto.newBuilder()
        .setId(user.getId())
        .setFirstName(user.getFirstName())
        .setLastName(user.getLastName())
        .setEmail(user.getEmail())
        .setPhoneNumber(user.getPhoneNumber())
        .setRole(user.getRole().name())
        .build();
  }
}
