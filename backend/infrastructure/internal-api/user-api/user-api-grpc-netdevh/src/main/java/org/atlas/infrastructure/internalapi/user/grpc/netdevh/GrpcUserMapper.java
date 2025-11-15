package org.atlas.infrastructure.internalapi.user.grpc.netdevh;

import java.util.List;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.ListUserResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.user.UserProto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrpcUserMapper {

  GrpcUserMapper INSTANCE = Mappers.getMapper(GrpcUserMapper.class);

  /**
   * Maps ListUserRequest to ListUserRequestProto
   */
  @Mapping(source = "ids", target = "idList")
  ListUserRequestProto map(ListUserRequest request);

  /**
   * Maps ListUserResponseProto to List of UserResponse
   */
  default List<UserResponse> map(ListUserResponseProto responseProto) {
    return ObjectMapperUtil.mapList(responseProto.getUserList(), this::map);
  }

  /**
   * Maps UserProto to UserResponse
   */
  @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
  UserResponse map(UserProto userProto);

  /**
   * Converts string role to Role enum
   */
  @Named("stringToRole")
  default Role stringToRole(String role) {
    return Role.valueOf(role);
  }
}