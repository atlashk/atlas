package org.atlas.services.iam.application.jwt.event.mapper;

import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEventMapper {

  UserEventMapper INSTANCE = Mappers.getMapper(UserEventMapper.class);

  @Mapping(target = "version", ignore = true)
  void merge(UserEntity user, @MappingTarget UserEvent event);
}
