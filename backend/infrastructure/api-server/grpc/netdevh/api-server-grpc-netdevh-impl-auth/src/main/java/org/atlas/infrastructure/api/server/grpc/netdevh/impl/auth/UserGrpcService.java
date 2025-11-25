package org.atlas.infrastructure.api.server.grpc.netdevh.impl.auth;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.domain.auth.usecase.user.handler.CreateUserUseCaseHandler;
import org.atlas.domain.auth.usecase.user.model.CreateUserInput;
import org.atlas.domain.user.shared.Role;
import org.atlas.infrastructure.api.server.grpc.protobuf.auth.CreateUserRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.auth.UserServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private final CreateUserUseCaseHandler createUserUseCaseHandler;

  @Override
  public void createUser(CreateUserRequestProto requestProto,
      StreamObserver<Empty> responseObserver) {
    CreateUserInput input = map(requestProto);
    createUserUseCaseHandler.handle(input);
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  private CreateUserInput map(CreateUserRequestProto requestProto) {
    return CreateUserInput.builder()
        .userId(requestProto.getUserId())
        .username(requestProto.getUsername())
        .password(requestProto.getPassword())
        .email(requestProto.getEmail())
        .phoneNumber(requestProto.getPhoneNumber())
        .role(Role.valueOf(requestProto.getRole()))
        .build();
  }
}
