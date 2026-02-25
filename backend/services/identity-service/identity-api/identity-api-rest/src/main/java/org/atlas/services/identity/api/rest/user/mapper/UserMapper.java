package org.atlas.services.identity.api.rest.user.mapper;

import org.atlas.services.identity.api.rest.authentication.model.ChangePasswordRequest;
import org.atlas.services.identity.api.rest.user.model.ProfileResponse;
import org.atlas.services.identity.api.rest.user.model.RegisterRequest;
import org.atlas.services.identity.port.in.authentication.model.ChangePasswordInput;
import org.atlas.services.identity.port.in.user.model.ProfileOutput;
import org.atlas.services.identity.port.in.user.model.RegisterInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  // Request --> Input
  // -----------------------------------------------------------------------------------------------

  RegisterInput toRegisterInput(RegisterRequest registerRequest);

  ChangePasswordInput toChangePasswordInput(ChangePasswordRequest request);

  // Output --> Response
  // -----------------------------------------------------------------------------------------------

  ProfileResponse toProfileResponse(ProfileOutput output);
}
