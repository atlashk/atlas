package org.atlas.infrastructure.api.server.rest.adapter.user.mapper;

import org.atlas.application.user.model.CreateUserInput;
import org.atlas.domain.user.entity.User;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.ProfileResponse;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  CreateUserInput toCreateUserInput(RegisterRequest registerRequest);

  ProfileResponse toProfileResponse(User user);
}
