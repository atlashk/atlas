package org.atlas.infrastructure.api.server.rest.impl.user.admin.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.infrastructure.api.server.rest.impl.user.admin.model.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  UserResponse toUserResponse(User user);
}
