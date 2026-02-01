package org.atlas.services.iam.infrastructure.api.server.rest.front.mapper;

import org.atlas.services.iam.infrastructure.api.server.rest.front.model.ProfileResponse;
import org.atlas.services.iam.infrastructure.api.server.rest.front.model.ChangePasswordRequest;
import org.atlas.services.iam.infrastructure.api.server.rest.front.model.RegisterRequest;
import org.atlas.services.iam.port.in.front.model.ChangePasswordInput;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  RegisterInput toRegisterInput(RegisterRequest registerRequest);

  ChangePasswordInput toChangePasswordInput(ChangePasswordRequest request);

  ProfileResponse toProfileResponse(ProfileOutput output);
}
