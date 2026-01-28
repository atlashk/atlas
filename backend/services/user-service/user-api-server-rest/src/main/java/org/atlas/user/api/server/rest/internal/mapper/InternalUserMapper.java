package org.atlas.user.api.server.rest.internal.mapper;

import org.atlas.user.application.internal.model.InternalRetrieveUserListInput;
import org.atlas.user.domain.entity.User;
import org.atlas.common.framework.internalapi.user.model.UserResponse;
import org.atlas.user.api.server.rest.internal.model.InternalRetrieveUserListRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalUserMapper {

  InternalUserMapper INSTANCE = Mappers.getMapper(InternalUserMapper.class);

  InternalRetrieveUserListInput toInternalRetrieveUserListInput(
      InternalRetrieveUserListRequest request);

  UserResponse toUserResponse(User user);
}
