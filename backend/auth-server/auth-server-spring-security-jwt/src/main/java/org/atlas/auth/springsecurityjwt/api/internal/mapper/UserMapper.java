package org.atlas.auth.springsecurityjwt.api.internal.mapper;

import org.atlas.auth.common.domain.entity.User;
import org.atlas.auth.springsecurityjwt.api.internal.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  User toUser(CreateUserRequest request);
}
