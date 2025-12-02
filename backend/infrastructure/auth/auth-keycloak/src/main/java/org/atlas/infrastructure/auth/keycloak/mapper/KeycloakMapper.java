package org.atlas.infrastructure.auth.keycloak.mapper;

import org.atlas.framework.auth.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KeycloakMapper {

  KeycloakMapper INSTANCE = Mappers.getMapper(KeycloakMapper.class);

  org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest toKeycloakCreateUserRequest(
      CreateUserRequest request);
}
