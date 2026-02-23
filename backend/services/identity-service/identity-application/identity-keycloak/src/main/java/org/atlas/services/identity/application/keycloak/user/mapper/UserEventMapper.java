package org.atlas.services.identity.application.keycloak.user.mapper;

import org.atlas.libs.framework.domain.common.event.contract.identity.UserCreatedEvent;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEventMapper {

  UserEventMapper INSTANCE = Mappers.getMapper(UserEventMapper.class);

  @Mapping(target = "version", ignore = true)
  void merge(UserEntity user, @MappingTarget UserCreatedEvent event);
}
