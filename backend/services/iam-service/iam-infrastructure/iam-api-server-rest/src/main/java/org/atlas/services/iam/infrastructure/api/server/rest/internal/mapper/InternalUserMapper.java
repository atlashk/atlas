package org.atlas.services.iam.infrastructure.api.server.rest.internal.mapper;

import org.atlas.libs.framework.internalapi.iam.model.UserResponse;
import org.atlas.services.iam.infrastructure.api.server.rest.internal.model.InternalRetrieveUserListRequest;
import org.atlas.services.iam.port.in.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.iam.port.in.internal.model.InternalUserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalUserMapper {

  InternalUserMapper INSTANCE = Mappers.getMapper(InternalUserMapper.class);

  @Mapping(source = "ids", target = "userIds")
  InternalRetrieveUserListInput toInternalRetrieveUserListInput(
      InternalRetrieveUserListRequest request);

  UserResponse toUserResponse(InternalUserOutput user);

  default String map(Integer value) {
    return value == null ? null : value.toString();
  }
}
