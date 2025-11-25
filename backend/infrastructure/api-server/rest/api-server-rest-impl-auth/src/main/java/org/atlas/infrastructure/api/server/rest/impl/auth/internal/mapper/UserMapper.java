package org.atlas.infrastructure.api.server.rest.impl.auth.internal.mapper;

import org.atlas.domain.auth.usecase.user.model.CreateUserInput;
import org.atlas.infrastructure.api.server.rest.impl.auth.internal.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  CreateUserInput toCreateUserInput(CreateUserRequest request);
}
