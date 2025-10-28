package org.atlas.infrastructure.api.server.rest.impl.user.front.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.ProfileResponse;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  RegisterInput toRegisterInput(RegisterRequest registerRequest);

  ProfileResponse toProfileResponse(User user);
}
