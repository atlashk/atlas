package org.atlas.infrastructure.auth.internal;

import org.atlas.framework.auth.model.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InternalAuthMapper {

  InternalAuthMapper INSTANCE = Mappers.getMapper(InternalAuthMapper.class);

  org.atlas.framework.internalapi.auth.model.CreateUserRequest toInternalCreateUserRequest(
      CreateUserRequest request);
}
