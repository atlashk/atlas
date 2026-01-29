package org.atlas.services.user.api.server.rest.mapper;

import org.atlas.services.user.api.server.rest.model.ProfileResponse;
import org.atlas.services.user.api.server.rest.model.RegisterRequest;
import org.atlas.services.user.application.model.CreateUserInput;
import org.atlas.services.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  CreateUserInput toCreateUserInput(RegisterRequest registerRequest);

  ProfileResponse toProfileResponse(User user);
}
