package org.atlas.platform.auth.jwt.api.internal.mapper;

import org.atlas.platform.auth.common.domain.entity.User;
import org.atlas.platform.auth.jwt.api.internal.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  User toUser(CreateUserRequest request);
}
