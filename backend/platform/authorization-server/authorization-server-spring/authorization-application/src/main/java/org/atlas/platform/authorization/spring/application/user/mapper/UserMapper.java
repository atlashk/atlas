package org.atlas.platform.authorization.spring.application.user.mapper;

import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.port.in.user.model.ProfileOutput;
import org.atlas.platform.authorization.port.in.user.model.RegisterInput;
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
  UserEntity toUser(RegisterInput input);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  @Mapping(source = "id", target = "userId")
  ProfileOutput toProfileOutput(UserEntity user);
}
