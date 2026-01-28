package org.atlas.auth.keycloak.api.internal.mapper;

import org.atlas.auth.keycloak.api.internal.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  org.atlas.common.infrastructure.iam.keycloak.model.CreateUserRequest toKeycloakCreateUserRequest(
      CreateUserRequest request);
}
