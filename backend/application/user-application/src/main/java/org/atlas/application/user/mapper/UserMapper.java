package org.atlas.application.user.mapper;

import org.atlas.application.user.model.CreateUserInput;
import org.atlas.domain.user.entity.User;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
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
