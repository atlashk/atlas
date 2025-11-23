package org.atlas.domain.user.usecase.front.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.auth.client.model.CreateAuthUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "plainPassword", source = "password")
  @Mapping(target = "password", ignore = true)
  User toUser(RegisterInput input);

  @Mapping(target = "userId", source = "id")
  CreateAuthUserRequest toCreateAuthUserRequest(User user);
}
