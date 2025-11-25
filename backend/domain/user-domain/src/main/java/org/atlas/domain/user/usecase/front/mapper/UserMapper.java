package org.atlas.domain.user.usecase.front.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  User toUser(RegisterInput input);

  @Mapping(target = "userId", source = "id")
  CreateUserRequest toCreateUserRequest(User user);
}
