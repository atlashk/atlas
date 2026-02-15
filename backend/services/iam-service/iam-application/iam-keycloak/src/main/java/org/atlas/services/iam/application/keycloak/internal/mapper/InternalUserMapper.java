package org.atlas.services.iam.application.keycloak.internal.mapper;

import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.user.model.admin.UserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalUserMapper {

  InternalUserMapper INSTANCE = Mappers.getMapper(InternalUserMapper.class);

  UserOutput toInternalUserOutput(UserEntity user);
}
