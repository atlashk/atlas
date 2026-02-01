package org.atlas.services.iam.application.jwt.internal.mapper;

import org.atlas.services.iam.domain.entity.User;
import org.atlas.services.iam.port.in.internal.model.InternalUserOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalUserMapper {

  InternalUserMapper INSTANCE = Mappers.getMapper(InternalUserMapper.class);

  InternalUserOutput toInternalUserOutput(User user);
}
