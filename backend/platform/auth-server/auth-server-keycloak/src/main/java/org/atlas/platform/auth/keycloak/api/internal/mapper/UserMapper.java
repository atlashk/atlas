package org.atlas.platform.auth.keycloak.api.internal.mapper;

import org.atlas.platform.auth.keycloak.api.internal.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  org.atlas.libs.iam.keycloak.model.CreateUserRequest toKeycloakCreateUserRequest(
      CreateUserRequest request);
}
