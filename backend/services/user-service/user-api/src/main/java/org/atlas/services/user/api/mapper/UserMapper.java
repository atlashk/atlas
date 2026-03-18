package org.atlas.services.user.api.mapper;

import org.atlas.services.user.port.in.model.ProfileOutput;
import org.atlas.services.user.port.in.model.RegisterInput;
import org.atlas.services.user.api.model.ProfileResponse;
import org.atlas.services.user.api.model.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  // Request --> Input
  // -----------------------------------------------------------------------------------------------

  RegisterInput toRegisterInput(RegisterRequest registerRequest);

  // Output --> Response
  // -----------------------------------------------------------------------------------------------

  ProfileResponse toProfileResponse(ProfileOutput output);
}
