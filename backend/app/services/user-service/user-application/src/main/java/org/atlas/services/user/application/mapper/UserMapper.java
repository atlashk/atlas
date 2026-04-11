package org.atlas.services.user.application.mapper;

import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.port.in.model.ProfileOutput;
import org.atlas.services.user.port.in.model.RegisterInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  // Input --> Entity
  // -----------------------------------------------------------------------------------------------

  @Mapping(target = "password", ignore = true)
  User toUser(RegisterInput input);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  @Mapping(source = "id", target = "userId")
  ProfileOutput toProfileOutput(User user);
}
