package org.atlas.infrastructure.api.server.rest.adapter.user.admin.mapper;

import org.atlas.domain.user.entity.User;
import org.atlas.infrastructure.api.server.rest.adapter.user.admin.model.AdminUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminUserMapper {

  AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

  AdminUserResponse toUserResponse(User user);
}
