package org.atlas.libs.internal.identity.grpc;

import java.util.List;
import org.atlas.libs.framework.domain.identity.UserRole;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.protobuf.identity.ListUserRequestProto;
import org.atlas.libs.protobuf.identity.ListUserResponseProto;
import org.atlas.libs.protobuf.identity.UserProto;
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
  default ListUserRequestProto map(RetrieveUserListInput request) {
    if (request == null) {
      return null;
    }

    ListUserRequestProto.Builder builder = ListUserRequestProto.newBuilder();
    if (CollectionUtil.isNotEmpty(request.getIds())) {
      builder.addAllId(request.getIds());
    }
    return builder.build();
  }

  /**
   * Maps ListUserResponseProto to List of UserResponse
   */
  default List<UserOutput> map(ListUserResponseProto responseProto) {
    return MapperUtil.mapList(responseProto.getUserList(), this::map);
  }

  /**
   * Maps UserProto to UserResponse
   */
  @Mapping(source = "role", target = "role", qualifiedByName = "stringToRole")
  UserOutput map(UserProto userProto);

  /**
   * Converts string role to Role enum
   */
  @Named("stringToRole")
  default UserRole stringToRole(String role) {
    return UserRole.valueOf(role);
  }
}
