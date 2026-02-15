package org.atlas.services.iam.application.jwt.user.mapper;

import org.atlas.libs.framework.internalapi.iam.model.UserOutput;
import org.atlas.services.iam.domain.entity.UserEntity;
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
