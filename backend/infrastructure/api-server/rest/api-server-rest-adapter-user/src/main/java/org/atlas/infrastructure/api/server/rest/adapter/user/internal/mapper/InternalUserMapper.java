package org.atlas.infrastructure.api.server.rest.adapter.user.internal.mapper;

import org.atlas.application.user.internal.model.InternalRetrieveUserListInput;
import org.atlas.domain.user.entity.User;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.infrastructure.api.server.rest.adapter.user.internal.model.InternalRetrieveUserListRequest;
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
