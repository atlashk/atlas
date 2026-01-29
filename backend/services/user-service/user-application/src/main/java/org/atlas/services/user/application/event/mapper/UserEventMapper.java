package org.atlas.services.user.application.event.mapper;

import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.services.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEventMapper {

  UserEventMapper INSTANCE = Mappers.getMapper(UserEventMapper.class);

  @Mapping(source = "id", target = "userId")
  void merge(User user, @MappingTarget UserEvent event);
}
