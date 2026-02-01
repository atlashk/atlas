package org.atlas.services.iam.application.jwt.front.mapper;

import org.atlas.services.iam.domain.entity.User;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  ProfileOutput toProfileOutput(User user);

  User toUser(RegisterInput input);
}
