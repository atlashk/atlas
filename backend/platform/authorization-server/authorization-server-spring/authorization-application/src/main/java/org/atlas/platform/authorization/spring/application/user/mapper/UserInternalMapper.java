package org.atlas.platform.authorization.spring.application.user.mapper;

import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserInternalMapper {

  UserInternalMapper INSTANCE = Mappers.getMapper(UserInternalMapper.class);

  // Entity --> Output
  // -----------------------------------------------------------------------------------------------

  UserOutput toUserOutput(UserEntity user);
}
