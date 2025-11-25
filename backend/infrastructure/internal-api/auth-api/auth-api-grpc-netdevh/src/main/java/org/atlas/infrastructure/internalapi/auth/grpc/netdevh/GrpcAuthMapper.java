package org.atlas.infrastructure.internalapi.auth.grpc.netdevh;

import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.atlas.infrastructure.api.server.grpc.protobuf.auth.CreateUserRequestProto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrpcAuthMapper {

  GrpcAuthMapper INSTANCE = Mappers.getMapper(GrpcAuthMapper.class);

  CreateUserRequestProto toCreateUserRequestProto(CreateUserRequest request);
}
