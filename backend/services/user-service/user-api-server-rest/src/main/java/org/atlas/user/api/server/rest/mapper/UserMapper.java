package org.atlas.user.api.server.rest.mapper;

import org.atlas.user.application.model.CreateUserInput;
import org.atlas.user.domain.entity.User;
import org.atlas.user.api.server.rest.model.ProfileResponse;
import org.atlas.user.api.server.rest.model.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  CreateUserInput toCreateUserInput(RegisterRequest registerRequest);

  ProfileResponse toProfileResponse(User user);
}
