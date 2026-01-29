package org.atlas.services.user.application.event.mapper;

import org.atlas.libs.framework.internalapi.auth.model.CreateUserRequest;
import org.atlas.services.user.application.model.CreateUserInput;
import org.atlas.services.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  User toUser(CreateUserInput input);

  @Mapping(target = "userId", source = "id")
  CreateUserRequest toCreateUserRequest(User user);
}
