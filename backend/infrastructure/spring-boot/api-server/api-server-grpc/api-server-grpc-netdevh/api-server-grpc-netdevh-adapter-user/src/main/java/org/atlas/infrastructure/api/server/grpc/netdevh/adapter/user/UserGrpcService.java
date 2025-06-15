package org.atlas.infrastructure.api.server.grpc.netdevh.adapter.user;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.collections4.CollectionUtils;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.usecase.internal.handler.InternalListUserUseCaseHandler;
import org.atlas.domain.user.usecase.internal.model.InternalListUserInput;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.UserProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.UserServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private final InternalListUserUseCaseHandler internalListUserUseCaseHandler;

  @Override
  public void listUser(ListUserRequestProto requestProto,
      StreamObserver<ListUserResponseProto> responseObserver) {
    InternalListUserInput input = map(requestProto);
    try {
      List<UserEntity> userEntities = internalListUserUseCaseHandler.handle(input);
      ListUserResponseProto responseProto = map(userEntities);
      responseObserver.onNext(responseProto);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalListUserInput map(ListUserRequestProto requestProto) {
    return new InternalListUserInput(requestProto.getIdList());
  }

  private ListUserResponseProto map(List<UserEntity> userEntities) {
    if (CollectionUtils.isEmpty(userEntities)) {
      return ListUserResponseProto.getDefaultInstance();
    }
    ListUserResponseProto.Builder builder = ListUserResponseProto.newBuilder();
    userEntities.forEach(user -> builder.addUser(map(user)));
    return builder.build();
  }

  private UserProto map(UserEntity user) {
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
