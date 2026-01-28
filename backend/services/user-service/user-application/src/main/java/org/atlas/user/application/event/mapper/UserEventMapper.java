package org.atlas.user.application.event.mapper;

import org.atlas.user.domain.entity.User;
import org.atlas.common.framework.domain.common.event.contract.user.UserEvent;
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
