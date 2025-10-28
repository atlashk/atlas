package org.atlas.infrastructure.api.server.rest.impl.user.internal.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.usecase.internal.model.InternalListUserInput;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.infrastructure.api.server.rest.impl.user.internal.model.InternalListUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InternalUserMapper {

  InternalUserMapper INSTANCE = Mappers.getMapper(InternalUserMapper.class);

  InternalListUserInput toInternalListUserInput(InternalListUserRequest internalListUserRequest);

  UserResponse toUserResponse(User user);
}
