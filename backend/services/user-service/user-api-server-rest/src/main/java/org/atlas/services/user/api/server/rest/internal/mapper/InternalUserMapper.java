package org.atlas.services.user.api.server.rest.internal.mapper;

import org.atlas.libs.framework.internalapi.user.model.UserResponse;
import org.atlas.services.user.api.server.rest.internal.model.InternalRetrieveUserListRequest;
import org.atlas.services.user.application.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.user.domain.entity.User;
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
